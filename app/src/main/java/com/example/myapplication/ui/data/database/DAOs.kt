package com.example.myapplication.ui.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow //

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM User WHERE id = :id")
    suspend fun getById(id: Int): User?

    @Query("SELECT * FROM User WHERE username = :username AND password = :password")
    suspend fun login(username: String, password: String): User?

    @Update
    suspend fun update(user: User)
}

@Dao
interface RequestDao {
    @Insert
    suspend fun insert(request: Request)

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


