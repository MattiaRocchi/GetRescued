package com.example.myapplication.ui.missions
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.GeneralMissionUser
import com.example.myapplication.data.database.WeeklyMissionUser
import com.example.myapplication.data.repositories.MissionRepository
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.TitleBadgeRepository
import com.example.myapplication.data.repositories.UserDaoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MissionViewModel(
    private val titleBadgeRepository: TitleBadgeRepository,
    private val userDaoRepository: UserDaoRepository,
    private val missionRepository: MissionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // UserId dallo store
    val userId = settingsRepository.userIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)
        /*
    // ✅ Flow delle missioni settimanali per un utente
    suspend fun getWeeklyMissions(): List<WeeklyMissionUser> {
        val id = userId.first() // prende il valore corrente dell'userIdFlow
        return if (id == -1) {
            emptyList()
        } else {
            missionRepository.getUserWeeklyMissions(id).first() // raccoglie il primo valore dal Flow
        }
    }

    // ✅ Flow delle missioni generali per un utente
    suspend fun getGeneralMissions(): List<GeneralMissionUser> {
        val id = userId.first() // prende il valore corrente dell'userIdFlow
        return if (id == -1) {
            emptyList()
        } else {
            missionRepository.getUserGeneralMissions(id).first() // raccoglie il primo valore dal Flow
        }
    }



    fun claimRewardGeneral(userId: Int, missionId: Int) {
        viewModelScope.launch {

            missionRepository.claimGeneralMission(userId, missionId)
            val newTitle = missionRepository.getMissionTitleById(missionId)
            if(newTitle != null) {
                titleBadgeRepository.insertUserBadgeCrossRef(userId, newTitle)
            }
        }
    }
    fun claimRewardWeekly(userId: Int, missionId: Int) {
        viewModelScope.launch {
            missionRepository.claimWeeklyMission(userId, missionId)
        }
    }
*/


}