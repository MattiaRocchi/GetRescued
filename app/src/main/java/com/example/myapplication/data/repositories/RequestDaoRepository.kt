package com.example.myapplication.data.repositories

import com.example.myapplication.data.database.PendingRequest
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.database.RequestDao
import com.example.myapplication.data.database.TagsRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RequestDaoRepository(private val requestDao: RequestDao) {

    suspend fun insertRequest(request: Request): Long = requestDao.insert(request) // Deve restituire Long
    suspend fun updateRequest(request: Request) = requestDao.update(request)
    suspend fun deleteRequest(request: Request) = requestDao.delete(request)
    suspend fun insertPendingRequest(pendingRequest: PendingRequest) = requestDao.insertPendingRequest(pendingRequest)
    suspend fun deletePendingRequest(pendingRequest: PendingRequest) = requestDao.deletePendingRequest(pendingRequest)
    suspend fun getPendingRequestsForRequest(requestId: Int) = requestDao.getPendingRequestsForRequest(requestId)
    suspend fun deletePendingRequest(requestId: Int, userId: Int) = requestDao.deletePendingRequest(requestId, userId)

    // Metodi per TagsRequest
    suspend fun insertTagsForRequest(vararg tagsRequest: TagsRequest) = requestDao.insertTagsForRequest(*tagsRequest)
    suspend fun deleteTagsForRequest(requestId: Int) = requestDao.deleteTagsForRequest(requestId)

    fun getAllRequests(): Flow<List<Request>> = requestDao.getAll()
    fun getOpenRequests(): Flow<List<Request>> = requestDao.getOpenRequests()
    fun getRequestsByUser(userId: Int): Flow<List<Request>> = requestDao.getRequestsByUser(userId)
    fun getAvailableRequestsForUser(userId: Int): Flow<List<Request>> = requestDao.getAvailableRequestsForUser(userId)
    fun getRequestByIdFlow(requestId: Int): Flow<Request?> = requestDao.getRequestByIdFlow(requestId)
    fun getRequestsParticipatingByUser(userId: Int): Flow<List<Request>> =
        getAllRequests().map { list -> list.filter { req -> userId in req.rescuers } }

    // Metodo per Stato forzato completato
    suspend fun markExpiredRequestsAsCompleted(currentTime: Long) =
        requestDao.markExpiredRequestsAsCompleted(currentTime)
    suspend fun getExpiredRequests(currentTime: Long): List<Request> =
        requestDao.getExpiredRequests(currentTime)
}