package com.example.myapplication.data.repositories

import com.example.myapplication.data.database.TagDao
import com.example.myapplication.data.database.Tags
import com.example.myapplication.data.database.TagsRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TagsRepository(private val tagDao: TagDao) {

    // Flow per UI reattiva
    fun allTagsFlow(): Flow<List<Tags>> = tagDao.getAllTags()
    fun getTagsForRequestFlow(requestId: Int): Flow<List<Tags>> = tagDao.getTagsForRequestFlow(requestId)

    // Wrapper suspend per snapshot (utile nel ViewModel quando vuoi assegnare List<Tags>)
    suspend fun getAll(): List<Tags> = tagDao.getAllTags().first()
    suspend fun getTagsForUser(userId: Int): List<Tags> = tagDao.getTagsForUser(userId).first()

    // Funzione per ottenere i tag di una richiesta
    suspend fun getTagsForRequest(requestId: Int): List<Tags> = tagDao.getTagsForRequest(requestId)

    // Verifica se un utente ha tutti i tag richiesti per una richiesta
    suspend fun userHasRequiredTags(userId: Int, requestId: Int): Boolean = tagDao.userHasRequiredTags(userId, requestId)
    suspend fun replaceUserTags(userId: Int, tagIds: List<Int>) = tagDao.replaceUserTags(userId, tagIds)

    // Operazioni per i tag delle richieste
    suspend fun insertTagsForRequest(vararg tr: TagsRequest) = tagDao.insertTagsForRequest(*tr)
    suspend fun deleteTagsForRequest(requestId: Int) = tagDao.deleteTagsForRequest(requestId)
}