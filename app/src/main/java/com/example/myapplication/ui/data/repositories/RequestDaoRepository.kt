package com.example.myapplication.ui.data.repositories

import androidx.room.Query
import com.example.myapplication.ui.data.database.Request
import com.example.myapplication.ui.data.database.RequestDao
import kotlinx.coroutines.flow.Flow

class RequestDaoRepository (private val requestDao: RequestDao) {

    fun getAllRequests(): Flow<List<Request>> = requestDao.getAll()

    fun getRequestsForUser(userId: Int): Flow<List<Request>> =
        requestDao.getAvailableRequestsForUser(userId)

    fun getOpenRequest(): Flow<List<Request>> = requestDao.getOpenRequests()

    //trova le richieste di aiuto mandate da uno specifico utente
    fun getRequestFromUser(userId: Int): Flow<List<Request>> =
        requestDao.getRequestsByUser(userId)


    suspend fun insertRequest(request: Request) = requestDao.insert(request)

    suspend fun updateRequest(request: Request) = requestDao.update(request)

    suspend fun deleteRequest(request: Request) = requestDao.delete(request)
}
/*@Dao
interface TripsDAO {
    @Query("SELECT * FROM trip ORDER BY name ASC")
    fun getAll(): Flow<List<Trip>>

    @Upsert
    suspend fun upsert(trip: Trip)

    @Delete
    suspend fun delete(item: Trip)
}

class TripsRepository(
    private val dao: TripsDAO
) {
    val trips: Flow<List<Trip>> = dao.getAll()

    suspend fun upsert(trip: Trip) = dao.upsert(trip)

    suspend fun delete(trip: Trip) = dao.delete(trip)
}
*/