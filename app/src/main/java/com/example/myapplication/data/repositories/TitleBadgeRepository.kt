package com.example.myapplication.data.repositories



import com.example.myapplication.data.database.TitleBadge
import com.example.myapplication.data.database.TitleBadgeDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TitleBadgeRepository(private val dao: TitleBadgeDao) {

    suspend fun insert(badge: TitleBadge) = dao.insert(badge)

    suspend fun getAll(): List<TitleBadge> = dao.getAll()

    fun getAllTitleBadges(): Flow<List<TitleBadge>> = flow { emit(dao.getAll()) }

    suspend fun getById(id: Int): TitleBadge? = dao.getById(id)

    suspend fun getActiveTitleByUserId(id: Int): TitleBadge? = dao.getActiveTitleByUserId(id)
    suspend fun updateActiveTitle(userId: Int, newTitle: Int) {
        dao.updateActiveTitle(userId, newTitle)
    }
    suspend fun getUserTitles (idUser: Int): List<TitleBadge> {
        return dao.getUserTitles(idUser)
    }
    suspend fun insertUserBadgeCrossRef(userId: Int, badgeId: Int) {
        dao.insertUserBadgeCrossRef(userId, badgeId)
    }

    suspend fun insertAll(titles: List<TitleBadge>) {
        dao.insertAll(titles)
    }
}