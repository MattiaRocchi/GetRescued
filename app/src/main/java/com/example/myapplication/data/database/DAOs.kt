
package com.example.myapplication.data.database

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

    @Query("SELECT * FROM Mission WHERE id = :id")
    suspend fun getById(id: Int): Mission?

    @Query("SELECT * FROM Mission")
    fun getAll(): Flow<List<Mission>>

    @Update
    suspend fun update(mission: Mission)

    @Delete
    suspend fun delete(mission: Mission)
}