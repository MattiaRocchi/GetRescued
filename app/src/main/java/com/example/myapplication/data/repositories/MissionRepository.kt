package com.example.myapplication.data.repositories


import com.example.myapplication.data.database.Mission
import com.example.myapplication.data.database.MissionDao

class MissionRepository(private val missionDao: MissionDao) {

    suspend fun getAllMissions() = missionDao.getAll()

    suspend fun insert(mission: Mission) = missionDao.insert(mission)

    suspend fun getById(id: Int) = missionDao.getById(id)

    suspend fun update(mission: Mission) = missionDao.update(mission)

    suspend fun delete(mission: Mission) = missionDao.delete(mission)

}