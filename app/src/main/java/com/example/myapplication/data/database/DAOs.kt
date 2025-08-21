
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
    suspend fun update(user: User)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long // Ritorna l'ID generato

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInfo(userInfo: UserInfo)

    @Transaction
    suspend fun insertUserWithInfo(user: User) {
        val userId = insert(user) // Inserisce e prende ID
        val info = UserInfo(
            id = userId.toInt(),
            activeTitle = 0,
            possessedTitles = listOf(0),
            exp = 0,
            profileFoto = null
        )
        insertInfo(info)
    }

    @Query("SELECT * FROM UserInfo WHERE id = :id")
    suspend fun getUserInfo(id: Int): UserInfo?

    @Query("UPDATE UserInfo SET profileFoto = :newFotoUri WHERE id = :id")
    suspend fun updateProfPic(id: Int, newFotoUri: String): Int

}

@Dao
interface RequestDao {
    @Insert
    suspend fun insert(request: Request)

    @Query("SELECT * FROM Request WHERE id = :requestId")
    suspend fun getRequestById(requestId: Int): Request?

    @Query("SELECT * FROM Request ORDER BY date DESC")
    fun getAll(): Flow<List<Request>>

    @Query("SELECT * FROM Request WHERE completed = 0")
    fun getOpenRequests(): Flow<List<Request>>

    @Query("SELECT * FROM Request WHERE sender = :userId")
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

    @Query("SELECT * FROM TitleBadge")
    suspend fun getAll(): List<TitleBadge>

    @Query("SELECT * FROM TitleBadge WHERE id = :id")
    suspend fun getById(id: Int): TitleBadge?
}
