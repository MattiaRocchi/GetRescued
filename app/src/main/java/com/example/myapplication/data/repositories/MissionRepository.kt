package com.example.myapplication.data.repositories


import com.example.myapplication.data.database.GeneralMissionUser
import com.example.myapplication.data.database.Mission
import com.example.myapplication.data.database.MissionDao
import com.example.myapplication.data.database.WeeklyMissionUser
import kotlinx.coroutines.flow.Flow

class MissionRepository(private val missionDao: MissionDao) {

    // Mission CRUD
    suspend fun getAllMissions() = missionDao.getAll()
    suspend fun insert(mission: Mission) = missionDao.insert(mission)
    suspend fun getById(id: Int) = missionDao.getById(id)
    suspend fun update(mission: Mission) = missionDao.update(mission)
    suspend fun delete(mission: Mission) = missionDao.delete(mission)

    // General Missions


    fun getUserGeneralMissions(userId: Int): Flow<List<GeneralMissionUser>> =
        missionDao.getUserGeneralMissions(userId)

    suspend fun userMissionsGeneral(userId: Int): List<Mission> =
        missionDao.userMissionsGeneral(userId)

    suspend fun getUserGeneralMissionsCompleted(userId: Int): List<Mission> =
        missionDao.getUserGeneralMissionsCompleted(userId)

    // Weekly Missions
    fun getUserWeeklyMissions(userId: Int): Flow<List<WeeklyMissionUser>> =
        missionDao.getUserWeeklyMissions(userId)

    suspend fun userMissionsWeekly(userId: Int): List<Mission> =
        missionDao.userMissionsWeekly(userId)

    suspend fun getUserWeeklyMissionsCompleted(userId: Int): List<Mission> =
        missionDao.getUserWeeklyMissionsCompleted(userId)

    // Reset & assign new missions
    suspend fun setWeeklyMissionsUser(userId: Int) {
        missionDao.deleteUserWeeklyMissions(userId)
        missionDao.setUserWeeklyMissions(userId)
    }

    suspend fun setGeneralMissionsUser(userId: Int) {
        missionDao.setUserGeneralMissions(userId)
    }


    suspend fun updateGeneralMissionUser(missionId: Int, userId: Int) {
        missionDao.updateGeneralMissionProgression(missionId, userId)
        if(missionDao.isGeneralCompleted(missionId, userId)) {
            missionDao.setGeneralMissionClaimable(missionId, userId)
        }
    }

    suspend fun updateWeeklyMissionUser(missionId: Int, userId: Int) {
        missionDao.updateWeeklyMissionProgression(missionId, userId)
        if(missionDao.isWeeklyCompleted(missionId, userId)) {
            missionDao.setGeneralMissionClaimable(missionId, userId)
        }
    }

    suspend fun claimGeneralMission(missionId: Int, userId: Int) {
        missionDao.claimExp(missionId, userId)

        missionDao.shutGeneralMission(missionId, userId)
    }

    suspend fun claimWeeklyMission(missionId: Int, userId: Int) {
        missionDao.claimExp(missionId, userId)

        missionDao.shutWeeklyMission(missionId, userId)
    }

    suspend fun getMissionTitleById(missionId: Int): Int? = missionDao.getMissionBadgeId(missionId)

}