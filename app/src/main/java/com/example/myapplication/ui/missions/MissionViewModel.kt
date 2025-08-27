package com.example.myapplication.ui.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.GeneralMissionUser
import com.example.myapplication.data.database.Mission
import com.example.myapplication.data.database.MissionUiState
import com.example.myapplication.data.database.WeeklyMissionUser
import com.example.myapplication.data.repositories.MissionRepository
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.TitleBadgeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class MissionViewModel(
    private val titleBadgeRepository: TitleBadgeRepository,
    private val missionRepository: MissionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // UserId dallo store
    private val userId = settingsRepository.userIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    private val _uiState = MutableStateFlow(MissionUiState())
    val uiState: StateFlow<MissionUiState> = _uiState.asStateFlow()

    init {
        loadMissions()
    }

    private fun loadMissions() {
        viewModelScope.launch {
            try {
                val currentUserId = userId.first()
                if (currentUserId == -1) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Utente non trovato"
                    )
                    return@launch
                }

                // Combine flows for reactive updates
                combine(
                    missionRepository.getUserGeneralMissions(currentUserId),
                    missionRepository.getUserWeeklyMissions(currentUserId)
                ) { generalMissionUsers, weeklyMissionUsers ->

                    // Load corresponding missions for general
                    val generalMissionsWithData = mutableListOf<Pair<Mission, GeneralMissionUser>>()
                    for (missionUser in generalMissionUsers) {
                        val mission = missionRepository.getById(missionUser.id)
                        if (mission != null) {
                            generalMissionsWithData.add(Pair(mission, missionUser))
                        }
                    }

                    // Load corresponding missions for weekly
                    val weeklyMissionsWithData = mutableListOf<Pair<Mission, WeeklyMissionUser>>()
                    for (missionUser in weeklyMissionUsers) {
                        val mission = missionRepository.getById(missionUser.id)
                        if (mission != null) {
                            weeklyMissionsWithData.add(Pair(mission, missionUser))
                        }
                    }

                    MissionUiState(
                        isLoading = false,
                        generalMissions = generalMissionsWithData,
                        weeklyMissions = weeklyMissionsWithData
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = MissionUiState()
                ).collect { newState ->
                    _uiState.value = newState
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Errore nel caricamento delle missioni: ${e.message}"
                )
            }
        }
    }

    fun claimGeneralMissionReward(missionId: Int) {
        viewModelScope.launch {
            try {
                val currentUserId = userId.first()
                if (currentUserId == -1) return@launch

                missionRepository.claimGeneralMission(missionId, currentUserId)

                // Check if mission has a title badge reward
                val newTitle = missionRepository.getMissionTitleById(missionId)
                if (newTitle != null) {
                    titleBadgeRepository.insertUserBadgeCrossRef(currentUserId, newTitle)
                }

                // Reload missions to reflect changes
                loadMissions()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Errore nel reclamare la ricompensa: ${e.message}"
                )
            }
        }
    }

    fun claimWeeklyMissionReward(missionId: Int) {
        viewModelScope.launch {
            try {
                val currentUserId = userId.first()
                if (currentUserId == -1) return@launch

                missionRepository.claimWeeklyMission(missionId, currentUserId)

                // Check if mission has a title badge reward
                val newTitle = missionRepository.getMissionTitleById(missionId)
                if (newTitle != null) {
                    titleBadgeRepository.insertUserBadgeCrossRef(currentUserId, newTitle)
                }

                // Reload missions to reflect changes
                loadMissions()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Errore nel reclamare la ricompensa: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}