
package com.example.myapplication.data.database

import android.nfc.Tag
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow //

@Dao
interface UserDao {

    @Query("SELECT * FROM User WHERE id = :id")
    suspend fun getById(id: Int): User?

    @Query("SELECT * FROM User WHERE email = :email")
    suspend fun getByEmail(email: String): User?

    @Query("SELECT * FROM User WHERE email = :email AND password = :password")
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM User WHERE email = :email")
    suspend fun findEmail(email: String): User?
    @Update
    suspend fun updateUser(user: User)
    @Update
    suspend fun updateUserInfo(userInfo: UserInfo)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long // Ritorna l'ID generato

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInfo(userInfo: UserInfo)

    @Transaction
    suspend fun insertUserWithInfo(user: User): Long {
        val userId = insert(user) // Inserisce e prende ID
        val info = UserInfo(
            id = userId.toInt(),
            activeTitle = 0,
            exp = 0,
            profileFoto = null
        )
        insertInfo(info)
        return userId
    }
    @Transaction
    suspend fun insertUserWithInfoChange(user: User, idOld: Int): Long {
        // Inserisce nuovo utente e ottiene nuovo ID
        val newUserId = insert(user)

        // Carica vecchia UserInfo, se esiste
        val oldInfo = getUserInfo(idOld)

        // Costruisce nuova UserInfo per il nuovo utente
        val newInfo = if (oldInfo == null) {
            UserInfo(
                id = newUserId.toInt(),
                activeTitle = 0,
                exp = 0,
                profileFoto = null
            )
        } else {
            // copia i dati ma cambia l'id con quello nuovo
            oldInfo.copy(id = newUserId.toInt())
        }

        // Elimina la vecchia UserInfo se esiste
        if (oldInfo != null) {
            deleteUserInfo(idOld)
        }

        // Inserisce la nuova UserInfo
        insertInfo(newInfo)

        return newUserId
    }
    @Query("DELETE FROM UserInfo WHERE id = :id")
    suspend fun deleteUserInfo(id: Int)

    @Query("SELECT * FROM UserInfo WHERE id = :id")
    suspend fun getUserInfo(id: Int): UserInfo?

    @Query("UPDATE UserInfo SET profileFoto = :newFotoUri WHERE id = :id")
    suspend fun updateProfPic(id: Int, newFotoUri: String): Int

    @Query("SELECT u.phoneNumber FROM User u WHERE id = :userId")
    suspend fun getUserPhoneNumber(userId: Int): String?

    @Query("SELECT u.created_at FROM User u WHERE id = :userId")
    suspend fun getUserCreation(userId: Int): Long
}

@Dao
interface RequestDao {
    @Insert
    suspend fun insert(request: Request)

    @Query("SELECT * FROM Request WHERE id in (:requestId) LIMIT 1")
    suspend fun getRequestById(requestId: Int): List<Request>

    @Query("SELECT * FROM Request WHERE id = :requestId LIMIT 1")
    fun getRequestByIdFlow(requestId: Int): Flow<Request?>

    @Query("SELECT * FROM Request ORDER BY date DESC")
    fun getAll(): Flow<List<Request>>

    @Query("SELECT * FROM Request WHERE completed = 0")
    fun getOpenRequests(): Flow<List<Request>>

    @Query("SELECT * FROM Request WHERE sender = :userId ORDER BY date DESC")
    fun getRequestsByUser(userId: Int): Flow<List<Request>>

    /*
    val requests: LiveData<List<Request>> = requestDao
    .getAvailableRequestsForUser(currentUserId)
    .asLiveData()
     */
    @Query("""
        SELECT DISTINCT r.* FROM Request r
        INNER JOIN TagsMission tm ON r.id = tm.idMissionId
        INNER JOIN TagsUser tu ON tm.idTags = tu.idTags
        WHERE tu.idUser = :userId AND r.completed = 0
    """)
    fun getAvailableRequestsForUser(userId: Int): Flow<List<Request>>


    @Update
    suspend fun update(request: Request)

    @Delete
    suspend fun delete(request: Request)

    @Query("""
    SELECT r.* 
    FROM UserPart u, Request r 
    WHERE r.id = u.idMissionId AND u.idUser = :userID
""")
    fun getUserRequests(userID: Int): Flow<List<Request>>

}

@Dao
interface TitleBadgeDao {
    @Insert
    suspend fun insert(badge: TitleBadge)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(titles: List<TitleBadge>)

    @Query("SELECT * FROM TitleBadge")
    suspend fun getAll(): List<TitleBadge>

    @Query("SELECT * FROM TitleBadge WHERE id = :id")
    suspend fun getById(id: Int): TitleBadge?

    @Query("SELECT t.* FROM TitleBadge t, UserInfo u WHERE u.id = :id and u.activeTitle = t.id LIMIT 1")
    suspend fun getActiveTitleByUserId(id: Int): TitleBadge?

    @Query("SELECT * FROM TitleBadge t, UserBadgeCrossRef u WHERE u.badgeId = t.id AND u.userId = :idUser")
    suspend fun getUserTitles(idUser: Int): List<TitleBadge>

    @Query("UPDATE UserInfo SET activeTitle = :newTitle WHERE id = :userId")
    suspend fun updateActiveTitle(userId: Int, newTitle: Int)

    @Query("INSERT INTO UserBadgeCrossRef (userId, badgeId) VALUES (:userId, :badgeId)")
    suspend fun insertUserBadgeCrossRef(userId: Int, badgeId: Int)
}
@Dao
interface MissionDao {
    @Insert
    suspend fun insert(mission: Mission)

    @Query("""
    INSERT INTO GeneralMissionUser(id, idUser, progression, active)
    SELECT id, :userId, 0, 1 FROM Mission WHERE type = 1
""")
    suspend fun setUserGeneralMissions(userId: Int)


    @Query("SELECT * FROM GeneralMissionUser WHERE idUser = :userId and active = 1")
    fun getUserGeneralMissions(userId: Int): Flow<List<GeneralMissionUser>>

    @Query("SELECT * FROM WeeklyMissionUser WHERE idUser = :userId and active = 1")
    fun getUserWeeklyMissions(userId: Int): Flow<List<WeeklyMissionUser>>

    @Query("SELECT m.* FROM GeneralMissionUser gm, Mission m WHERE idUser = :userId and m.id = gm.id and gm.active = 1")
    suspend fun userMissionsGeneral(userId: Int): List<Mission>

    @Query("SELECT m.* FROM WeeklyMissionUser wm, Mission m WHERE idUser = :userId and m.id = wm.id and wm.active = 1")
    suspend fun userMissionsWeekly(userId: Int): List<Mission>




    @Query("""SELECT m.*
                    FROM Mission m, GeneralMissionUser gm
                    WHERE gm.idUser = :userId AND gm.active = 0 AND m.type = 1""")
    suspend fun getUserGeneralMissionsCompleted(userId: Int): List<Mission>

    @Query("""SELECT m.*
                    FROM Mission m, WeeklyMissionUser wm
                    WHERE wm.idUser = :userId AND wm.active = 0 AND m.type = 0""")
    suspend fun getUserWeeklyMissionsCompleted(userId: Int): List<Mission>

    @Query("""

    INSERT INTO WeeklyMissionUser(id, idUser, progression, active)
    SELECT m.id, :userId, 0, 1
    FROM Mission m
    JOIN TagsMission tm ON m.id = tm.idMissionId
    JOIN TagsUser tu ON tm.idTags = tu.idTags
    WHERE tu.idUser = :userId AND m.type = 0
    ORDER BY RANDOM() 
    LIMIT 3
""")
    suspend fun setUserWeeklyMissions(userId: Int)

    @Query("    Delete FROM WeeklyMissionUser WHERE idUser = :userId ")
    suspend fun deleteUserWeeklyMissions(userId: Int)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mission: List<Mission>)

    @Query("SELECT * FROM Mission WHERE id = :id")
    suspend fun getById(id: Int): Mission?

    @Query("SELECT * FROM Mission")
    fun getAll(): List<Mission>
    @Query("SELECT COUNT(*) FROM Mission")
    suspend fun getMissionCount(): Int
    @Query("""Update GeneralMissionUser set progression = progression+1 
                WHERE id = :missionId AND idUser = :userId AND active = 1 AND claimable = 0""")
    suspend fun updateGeneralMissionProgression(missionId: Int, userId: Int)

    @Query("""Update WeeklyMissionUser set progression = progression+1 
                  WHERE id = :missionId AND idUser = :userId AND active = 1 AND claimable = 0""")
    suspend fun updateWeeklyMissionProgression(missionId: Int, userId: Int)

    @Query("""Update GeneralMissionUser set active = 0 and claimable = 0 
        WHERE id = :missionId AND idUser = :userId""")
    suspend fun shutGeneralMission(missionId: Int, userId: Int)

    @Query("""Update WeeklyMissionUser set active = 0 and claimable = 0
                WHERE id = :missionId AND idUser = :userId""")
    suspend fun shutWeeklyMission(missionId: Int, userId: Int)

    @Query("""Update GeneralMissionUser set claimable = 1 
                WHERE id = :missionId AND idUser = :userId""")
    suspend fun setGeneralMissionClaimable(missionId: Int, userId: Int)

    @Query("""Update WeeklyMissionUser set claimable = 1 
                WHERE id = :missionId AND idUser = :userId""")
    suspend fun setWeeklyMissionClaimable(missionId: Int, userId: Int)
    @Query("""
        SELECT CASE 
            WHEN wm.progression >= m.requirement THEN 1 
            ELSE 0 
        END 
        FROM WeeklyMissionUser wm
        INNER JOIN Mission m ON wm.id = m.id
        WHERE wm.idUser = :userId AND wm.id = :missionId
    """)
    suspend fun isWeeklyCompleted(missionId: Int, userId: Int ): Boolean

    @Query("""
        SELECT CASE 
            WHEN gm.progression >= m.requirement THEN 1 
            ELSE 0 
        END 
        FROM GeneralMissionUser gm
        INNER JOIN Mission m ON gm.id = m.id
        WHERE gm.idUser = :userId AND gm.id = :missionId
    """)
    suspend fun isGeneralCompleted(missionId: Int, userId: Int): Boolean

    @Query("SELECT exp FROM Mission WHERE id = :missionId")
    suspend fun getMissionExp(missionId: Int): Int

    @Query("UPDATE UserInfo SET exp = exp + :exp WHERE id = :userId")
    suspend fun addUserExp(userId: Int, exp: Int)

    @Transaction
    suspend fun claimExp(missionId: Int, userId: Int) {
        val exp = getMissionExp(missionId)
        addUserExp(userId, exp)
    }

    @Query("SELECT m.titleBadge FROM Mission m WHERE id = :missionId")
    suspend fun getMissionBadgeId(missionId: Int): Int?

    @Update
    suspend fun update(mission: Mission)

    @Delete
    suspend fun delete(mission: Mission)
}

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tags: List<Tags>)

    @Query("SELECT * FROM Tags ORDER BY name")
    fun getAllTags(): Flow<List<Tags>>

    @Transaction
    @Query("""
        SELECT t.* FROM Tags t
        INNER JOIN TagsUser tu ON t.id = tu.idTags
        WHERE tu.idUser = :userId
        ORDER BY t.name
    """)
    fun getTagsForUser(userId: Int): Flow<List<Tags>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUserTags(vararg tagUser: TagsUser)

    @Query("DELETE FROM TagsUser WHERE idUser = :userId")
    suspend fun deleteAllTagsForUser(userId: Int)

    // mission joins...
    @Transaction
    @Query("""
        SELECT t.* FROM Tags t
        INNER JOIN TagsMission tm ON t.id = tm.idTags
        WHERE tm.idMissionId = :missionId
        ORDER BY t.name
    """)
    fun getTagsForMission(missionId: Int): Flow<List<Tags>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagsForMission(vararg tm: TagsMission)

    @Query("DELETE FROM TagsMission WHERE idMissionId = :missionId")
    suspend fun deleteTagsForMission(missionId: Int)

    @Transaction
    suspend fun replaceUserTags(userId: Int, tagIds: List<Int>) {
        deleteAllTagsForUser(userId)
        if (tagIds.isNotEmpty()) {
            val list = tagIds.map { TagsUser(idTags = it, idUser = userId) }
            insertUserTags(*list.toTypedArray())
        }
    }


}
