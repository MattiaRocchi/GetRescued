package com.example.myapplication.data.repositories



import com.example.myapplication.data.database.TitleBadge
import com.example.myapplication.data.database.TitleBadgeDao

class TitleBadgeRepository(private val dao: TitleBadgeDao) {

    suspend fun insert(badge: TitleBadge) = dao.insert(badge)

    suspend fun getAll(): List<TitleBadge> = dao.getAll()

    suspend fun getById(id: Int): TitleBadge? = dao.getById(id)
}