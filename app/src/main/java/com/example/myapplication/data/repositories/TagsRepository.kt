package com.example.myapplication.data.repositories

import com.example.myapplication.data.database.TagDao
import com.example.myapplication.data.database.Tags
import com.example.myapplication.data.database.TagsMission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TagsRepository(private val tagDao: TagDao) {

    // Flow per UI reattiva
    fun allTagsFlow(): Flow<List<Tags>> = tagDao.getAllTags()
    fun tagsForUserFlow(userId: Int): Flow<List<Tags>> = tagDao.getTagsForUser(userId)
    fun tagsForMissionFlow(missionId: Int): Flow<List<Tags>> = tagDao.getTagsForMission(missionId)

    // Wrapper suspend per snapshot (utile nel ViewModel quando vuoi assegnare List<Tags>)
    suspend fun getAll(): List<Tags> = tagDao.getAllTags().first()
    suspend fun getTagsForUser(userId: Int): List<Tags> = tagDao.getTagsForUser(userId).first()
    suspend fun getTagsForMission(missionId: Int): List<Tags> = tagDao.getTagsForMission(missionId).first()

    // Operazioni di mutazione
    suspend fun insertAll(tags: List<Tags>) = tagDao.insertAll(tags)
    suspend fun replaceUserTags(userId: Int, tagIds: List<Int>) = tagDao.replaceUserTags(userId, tagIds)
    suspend fun deleteAllTagsForUser(userId: Int) = tagDao.deleteAllTagsForUser(userId)

    suspend fun insertTagsForMission(vararg tm: TagsMission) = tagDao.insertTagsForMission(*tm)
    suspend fun deleteTagsForMission(missionId: Int) = tagDao.deleteTagsForMission(missionId)
}
