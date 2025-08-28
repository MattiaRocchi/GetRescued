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

import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class MissionViewModel(
    private val titleBadgeRepository: TitleBadgeRepository,
    private val missionRepository: MissionRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    // UserId dallo store - stesso pattern del ProfileViewModel
    private val userId = settingsRepository.userIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    private val _uiState = MutableStateFlow(MissionUiState())
    val uiState: StateFlow<MissionUiState> = _uiState.asStateFlow()

    init {
        // Stesso pattern del ProfileViewModel: osserva i cambiamenti di userId
        viewModelScope.launch {
            userId.collect { id ->
                if (id != -1) {
                    loadMissionsForUser(id)
                } else {
                    // Reset se non loggato
                    _uiState.value = MissionUiState(
                        isLoading = false,
                        generalMissions = emptyList(),
                        weeklyMissions = emptyList(),
                        error = "Utente non trovato"
                    )
                }
            }
        }
    }

    private fun loadMissionsForUser(currentUserId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                // Combine flows per aggiornamenti reattivi
                combine(
                    missionRepository.getUserGeneralMissions(currentUserId),
                    missionRepository.getUserWeeklyMissions(currentUserId)
                ) { generalMissionUsers, weeklyMissionUsers ->

                    // Load missioni generali con dati
                    val generalMissionsWithData = mutableListOf<Pair<Mission, GeneralMissionUser>>()
                    for (missionUser in generalMissionUsers) {
                        val mission = missionRepository.getById(missionUser.id)
                        if (mission != null) {
                            generalMissionsWithData.add(Pair(mission, missionUser))
                        }
                    }

                    // Load missioni settimanali con dati
                    val weeklyMissionsWithData = mutableListOf<Pair<Mission, WeeklyMissionUser>>()
                    for (missionUser in weeklyMissionUsers) {
                        val mission = missionRepository.getById(missionUser.id)
                        if (mission != null) {
                            weeklyMissionsWithData.add(Pair(mission, missionUser))
                        }
                    }

                    // Ordina le missioni: prima quelle attive/claimable, poi quelle completate
                    val sortedGeneralMissions = generalMissionsWithData.sortedWith(
                        compareBy<Pair<Mission, GeneralMissionUser>> { (_, missionUser) ->
                            // Le missioni completate (active=false && claimable=false) vanno in fondo
                            if (!missionUser.active && !missionUser.claimable) 1 else 0
                        }.thenBy { (_, missionUser) ->
                            // Tra quelle attive, prima quelle claimable
                            if (missionUser.claimable) 0 else 1
                        }.thenBy { (mission, _) ->
                            // Infine ordine alfabetico per nome
                            mission.name
                        }
                    )

                    val sortedWeeklyMissions = weeklyMissionsWithData.sortedWith(
                        compareBy<Pair<Mission, WeeklyMissionUser>> { (_, missionUser) ->
                            // Le missioni completate (active=false && claimable=false) vanno in fondo
                            if (!missionUser.active && !missionUser.claimable) 1 else 0
                        }.thenBy { (_, missionUser) ->
                            // Tra quelle attive, prima quelle claimable
                            if (missionUser.claimable) 0 else 1
                        }.thenBy { (mission, _) ->
                            // Infine ordine alfabetico per nome
                            mission.name
                        }
                    )

                    MissionUiState(
                        isLoading = false,
                        generalMissions = sortedGeneralMissions,
                        weeklyMissions = sortedWeeklyMissions,
                        error = null
                    )
                }.collect { newState ->
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
                val currentUserId = userId.value
                if (currentUserId == -1) return@launch

                missionRepository.claimGeneralMission(missionId, currentUserId)

                // Check if mission has a title badge reward
                val newTitle = missionRepository.getMissionTitleById(missionId)
                if (newTitle != null) {
                    titleBadgeRepository.insertUserBadgeCrossRef(currentUserId, newTitle)
                }

                // Le missioni si ricaricano automaticamente tramite il Flow in loadMissionsForUser
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
                val currentUserId = userId.value
                if (currentUserId == -1) return@launch

                missionRepository.claimWeeklyMission(missionId, currentUserId)

                // Check if mission has a title badge reward
                val newTitle = missionRepository.getMissionTitleById(missionId)
                if (newTitle != null) {
                    titleBadgeRepository.insertUserBadgeCrossRef(currentUserId, newTitle)
                }

                // Le missioni si ricaricano automaticamente tramite il Flow in loadMissionsForUser
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