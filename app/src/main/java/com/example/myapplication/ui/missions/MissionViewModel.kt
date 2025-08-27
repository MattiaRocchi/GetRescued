package com.example.myapplication.ui.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.GeneralMissionUser
import com.example.myapplication.data.database.Mission
import com.example.myapplication.data.database.WeeklyMissionUser
import com.example.myapplication.data.repositories.MissionRepository
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.TitleBadgeRepository
import com.example.myapplication.data.repositories.UserDaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Data class per combinare Mission con i suoi dati utente
data class MissionWithProgress(
    val mission: Mission,
    val generalMissionUser: GeneralMissionUser? = null,
    val weeklyMissionUser: WeeklyMissionUser? = null,
    val isCompleted: Boolean = false,
    val canClaim: Boolean = false
) {
    val progression: Int
        get() = generalMissionUser?.progression ?: weeklyMissionUser?.progression ?: 0

    val isActive: Boolean
        get() = generalMissionUser?.active ?: weeklyMissionUser?.active ?: false

    val isClaimable: Boolean
        get() = generalMissionUser?.claimable ?: weeklyMissionUser?.claimable ?: false
}

class MissionViewModel(
    private val titleBadgeRepository: TitleBadgeRepository,
    private val userDaoRepository: UserDaoRepository,
    private val missionRepository: MissionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // UserId dallo store
    val userId = settingsRepository.userIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    // Stati per le missioni generali
    private val _generalMissions = MutableStateFlow<List<MissionWithProgress>>(emptyList())
    val generalMissions: StateFlow<List<MissionWithProgress>> = _generalMissions

    // Stati per le missioni settimanali
    private val _weeklyMissions = MutableStateFlow<List<MissionWithProgress>>(emptyList())
    val weeklyMissions: StateFlow<List<MissionWithProgress>> = _weeklyMissions

    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        // Osserva i cambiamenti dell'userId e ricarica le missioni
        viewModelScope.launch {
            userId.collect { id ->
                if (id != -1) {
                    loadMissions(id)
                } else {
                    // Reset se non loggato
                    _generalMissions.value = emptyList()
                    _weeklyMissions.value = emptyList()
                }
            }
        }
    }

    /**
     * Carica tutte le missioni per l'utente specificato
     */
    private suspend fun loadMissions(userId: Int) {
        try {
            _isLoading.value = true
            _errorMessage.value = null

            loadGeneralMissions(userId)
            loadWeeklyMissions(userId)

        } catch (e: Exception) {
            _errorMessage.value = "Errore nel caricamento delle missioni: ${e.message}"
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Carica le missioni generali dell'utente
     */
    private suspend fun loadGeneralMissions(userId: Int) {
        viewModelScope.launch {
            missionRepository.getUserGeneralMissions(userId).collect { generalMissionUsers ->
                val missions = mutableListOf<MissionWithProgress>()

                for (generalMissionUser in generalMissionUsers) {
                    val mission = missionRepository.getById(generalMissionUser.id)
                    mission?.let { m ->
                        val isCompleted = generalMissionUser.progression >= m.requirement
                        val canClaim = isCompleted && generalMissionUser.active && generalMissionUser.claimable

                        missions.add(
                            MissionWithProgress(
                                mission = m,
                                generalMissionUser = generalMissionUser,
                                weeklyMissionUser = null,
                                isCompleted = isCompleted,
                                canClaim = canClaim
                            )
                        )
                    }
                }

                _generalMissions.value = missions
            }
        }
    }

    /**
     * Carica le missioni settimanali dell'utente
     */
    private suspend fun loadWeeklyMissions(userId: Int) {
        viewModelScope.launch {
            missionRepository.getUserWeeklyMissions(userId).collect { weeklyMissionUsers ->
                val missions = mutableListOf<MissionWithProgress>()

                for (weeklyMissionUser in weeklyMissionUsers) {
                    val mission = missionRepository.getById(weeklyMissionUser.id)
                    mission?.let { m ->
                        val isCompleted = weeklyMissionUser.progression >= m.requirement
                        val canClaim = isCompleted && weeklyMissionUser.active && weeklyMissionUser.claimable

                        missions.add(
                            MissionWithProgress(
                                mission = m,
                                generalMissionUser = null,
                                weeklyMissionUser = weeklyMissionUser,
                                isCompleted = isCompleted,
                                canClaim = canClaim
                            )
                        )
                    }
                }

                _weeklyMissions.value = missions
            }
        }
    }

    /**
     * Ricarica manualmente le missioni (utile per pull-to-refresh)
     */
    fun refreshMissions() {
        viewModelScope.launch {
            val id = userId.value
            if (id != -1) {
                loadMissions(id)
            }
        }
    }

    /**
     * Claim reward per una missione generale
     */
    fun claimGeneralMission(missionId: Int) {
        viewModelScope.launch {
            try {
                val id = userId.value
                if (id == -1) return@launch

                missionRepository.claimGeneralMission(missionId, id)

                // Controlla se c'è un titolo da assegnare
                val newTitle = missionRepository.getMissionTitleById(missionId)
                newTitle?.let { titleId ->
                    titleBadgeRepository.insertUserBadgeCrossRef(id, titleId)
                }

                // Ricarica le missioni per aggiornare l'UI
                loadGeneralMissions(id)

            } catch (e: Exception) {
                _errorMessage.value = "Errore nel claim della missione: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    /**
     * Claim reward per una missione settimanale
     */
    fun claimWeeklyMission(missionId: Int) {
        viewModelScope.launch {
            try {
                val id = userId.value
                if (id == -1) return@launch

                missionRepository.claimWeeklyMission(missionId, id)

                // Ricarica le missioni per aggiornare l'UI
                loadWeeklyMissions(id)

            } catch (e: Exception) {
                _errorMessage.value = "Errore nel claim della missione: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    /**
     * Utility per ottenere una missione specifica per ID
     */
    fun getMissionById(missionId: Int): MissionWithProgress? {
        val allMissions = _generalMissions.value + _weeklyMissions.value
        return allMissions.find { it.mission.id == missionId }
    }

    /**
     * Pulisce il messaggio di errore
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Inizializza le missioni generali per un nuovo utente
     */
    fun initializeGeneralMissionsForUser() {
        viewModelScope.launch {
            try {
                val id = userId.value
                if (id != -1) {
                    missionRepository.setGeneralMissionsUser(id)
                    loadGeneralMissions(id)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Errore nell'inizializzazione delle missioni generali: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    /**
     * Reset e assegna nuove missioni settimanali
     */
    fun resetAndAssignWeeklyMissions() {
        viewModelScope.launch {
            try {
                val id = userId.value
                if (id != -1) {
                    missionRepository.setWeeklyMissionsUser(id)
                    loadWeeklyMissions(id)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Errore nel reset delle missioni settimanali: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    /**
     * Aggiorna il progresso di una missione generale
     * (da chiamare quando l'utente completa un'azione che fa progredire una missione)
     */
    fun updateGeneralMissionProgress(missionId: Int) {
        viewModelScope.launch {
            try {
                val id = userId.value
                if (id != -1) {
                    missionRepository.updateGeneralMissionUser(missionId, id)
                    loadGeneralMissions(id)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Errore nell'aggiornamento progresso missione generale: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    /**
     * Aggiorna il progresso di una missione settimanale
     */
    fun updateWeeklyMissionProgress(missionId: Int) {
        viewModelScope.launch {
            try {
                val id = userId.value
                if (id != -1) {
                    missionRepository.updateWeeklyMissionUser(missionId, id)
                    loadWeeklyMissions(id)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Errore nell'aggiornamento progresso missione settimanale: ${e.message}"
                e.printStackTrace()
            }
        }
    }
}