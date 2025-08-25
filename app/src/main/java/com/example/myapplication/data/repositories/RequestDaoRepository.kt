package com.example.myapplication.data.repositories

import com.example.myapplication.data.database.Request
import com.example.myapplication.data.database.RequestDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RequestDaoRepository (private val requestDao: RequestDao) {


    suspend fun insertRequest(request: Request) = requestDao.insert(request)
    suspend fun updateRequest(request: Request) = requestDao.update(request)
    suspend fun deleteRequest(request: Request) = requestDao.delete(request)

    fun getAllRequests(): Flow<List<Request>> = requestDao.getAll()
    fun getOpenRequests(): Flow<List<Request>> = requestDao.getOpenRequests()
    fun getRequestsByUser(userId: Int): Flow<List<Request>> = requestDao.getRequestsByUser(userId)
    fun getAvailableRequestsForUser(userId: Int): Flow<List<Request>> = requestDao.getAvailableRequestsForUser(userId)
    fun getRequestByIdFlow(requestId: Int): Flow<Request?> = requestDao.getRequestByIdFlow(requestId)
    fun getRequestsParticipatingByUser(userId: Int): Flow<List<Request>> =
        getAllRequests().map { list -> list.filter { req -> userId in req.rescuers } }
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