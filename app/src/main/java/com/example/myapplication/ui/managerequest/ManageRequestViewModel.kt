package com.example.myapplication.ui.managerequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.database.Tags
import com.example.myapplication.data.database.UserWithInfo
import com.example.myapplication.data.repositories.MissionRepository
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.TagsRepository
import com.example.myapplication.data.repositories.UserDaoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ManageRequestViewModel(
    private val requestRepository: RequestDaoRepository,
    private val userDaoRepository: UserDaoRepository,
    private val settingsRepository: SettingsRepository,
    private val tagsRepository: TagsRepository,
    private val missionRepository: MissionRepository,
    private val requestId: Int
) : ViewModel() {

    private suspend fun checkAndMarkExpiredRequests() {
        try {
            val currentTime = System.currentTimeMillis()
            requestRepository.markExpiredRequestsAsCompleted(currentTime)
        } catch (e: Exception) {
            _events.tryEmit("Errore nel controllo richieste scadute: ${e.message}")
        }
    }

    sealed class UiState {
        object Loading : UiState()
        object NotFound : UiState()
        object NotAuthorized : UiState()
        data class Ready(
            val request: Request,
            val pendingParticipants: List<UserWithInfo> = emptyList(),
            val approvedParticipants: List<UserWithInfo> = emptyList(),
            val requestTags: List<Tags> = emptyList()
        ) : UiState() {
            // Funzioni di utilità per determinare le azioni disponibili
            val canDelete: Boolean get() = request.canBeDeleted()
            val canEdit: Boolean get() = request.canBeModified()
            val canMarkCompleted: Boolean get() = request.isScheduledForToday() || request.isScheduledInPast()
            val canManageParticipants: Boolean get() = request.canBeModified()
            val isExpired: Boolean get() = request.isScheduledInPast()

            val isComplete: Boolean get() = request.completed
            val isInPreparation: Boolean get() = request.isScheduledForTomorrow()
            val requestState: String get() = request.getRequestState()
        }
    }

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val requestFlow: Flow<Request?> = requestRepository.getRequestByIdFlow(requestId)
    private val userIdFlow: Flow<Int> = settingsRepository.userIdFlow

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Modifica l'init per includere il controllo delle richieste scadute
    init {
        viewModelScope.launch {
            // Prima controlla e marca le richieste scadute
            //checkAndMarkExpiredRequests()

            // Poi procedi con il normale caricamento
            combine(requestFlow, userIdFlow) { req, uid ->
                when {
                    req == null -> UiState.NotFound
                    uid <= 0 || uid != req.sender -> UiState.NotAuthorized
                    else -> {
                        UiState.Ready(
                            request = req,
                            pendingParticipants = emptyList(),
                            approvedParticipants = emptyList(),
                            requestTags = emptyList()
                        )
                    }
                }
            }
                .distinctUntilChanged()
                .collect { state ->
                    _uiState.value = state

                    if (state is UiState.Ready) {
                        loadParticipants(state.request)
                    }
                }
        }
    }

    private suspend fun loadParticipants(request: Request) {
        try {
            // Carica i dati degli utenti approvati
            val approvedUsers = mutableListOf<UserWithInfo>()
            request.rescuers.forEach { userId ->
                userDaoRepository.getUserWithInfo(userId)?.let { user ->
                    approvedUsers.add(user)
                }
            }

            // Carica i dati degli utenti con richieste pending
            val pendingUsers = mutableListOf<UserWithInfo>()
            val pendingRequests = requestRepository.getPendingRequestsForRequest(request.id)
            pendingRequests.forEach { pendingRequest ->
                userDaoRepository.getUserWithInfo(pendingRequest.userId)?.let { user ->
                    pendingUsers.add(user)
                }
            }

            // Carica i tag della richiesta
            val tags = tagsRepository.getTagsForRequest(request.id)

            _uiState.update { current ->
                if (current is UiState.Ready) {
                    current.copy(
                        approvedParticipants = approvedUsers,
                        pendingParticipants = pendingUsers,
                        requestTags = tags
                    )
                } else current
            }
        } catch (e: Exception) {
            _events.tryEmit("Errore nel caricamento dei partecipanti: ${e.message}")
        }
    }

    // Modifica approveParticipant
    fun approveParticipant(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _uiState.value
            if (current !is UiState.Ready) return@launch

            val request = current.request

            if (userId in request.rescuers) {
                _events.tryEmit("Questo utente è già approvato")
                return@launch
            }

            if (request.rescuers.size >= request.peopleRequired) {
                _events.tryEmit("Non ci sono più posti disponibili")
                return@launch
            }

            try {
                val updatedRequest = request.copy(rescuers = request.rescuers + userId)
                requestRepository.updateRequest(updatedRequest)
                requestRepository.deletePendingRequest(request.id, userId)
                _events.tryEmit("Partecipante approvato con successo!")

                // REFRESH DOPO IL SUCCESSO
                refreshRequest()

            } catch (e: Exception) {
                _events.tryEmit("Errore nell'approvazione: ${e.message}")
            }
        }
    }

    // Modifica rejectParticipant
    fun rejectParticipant(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _uiState.value
            if (current !is UiState.Ready) return@launch

            try {
                requestRepository.deletePendingRequest(current.request.id, userId)
                _events.tryEmit("Richiesta di partecipazione rifiutata")

                // REFRESH DOPO IL SUCCESSO
                refreshRequest()

            } catch (e: Exception) {
                _events.tryEmit("Errore nel rifiuto: ${e.message}")
            }
        }
    }

    // Modifica removeParticipant
    fun removeParticipant(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _uiState.value
            if (current !is UiState.Ready) return@launch

            val request = current.request

            if (userId !in request.rescuers) {
                _events.tryEmit("Questo utente non è tra i partecipanti approvati")
                return@launch
            }

            try {
                val updatedRequest = request.copy(rescuers = request.rescuers - userId)
                requestRepository.updateRequest(updatedRequest)
                _events.tryEmit("Partecipante rimosso dalla richiesta")

                // REFRESH DOPO IL SUCCESSO
                refreshRequest()

            } catch (e: Exception) {
                _events.tryEmit("Errore nella rimozione: ${e.message}")
            }
        }
    }

    fun deleteRequest(onDeleted: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _uiState.value
            if (current !is UiState.Ready) return@launch

            val request = current.request

            if (!request.canBeDeleted()) {
                _events.tryEmit("Impossibile eliminare: la richiesta è troppo vicina alla data di svolgimento")
                return@launch
            }

            try {
                requestRepository.deleteRequest(request)
                _events.tryEmit("Richiesta eliminata con successo")
                onDeleted()
            } catch (e: Exception) {
                _events.tryEmit("Errore nell'eliminazione: ${e.message}")
            }
        }
    }

    // Modifica il metodo markAsCompleted per includere l'aggiornamento delle missioni
    fun markAsCompleted(onCompleted: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _uiState.value
            if (current !is UiState.Ready) return@launch

            val request = current.request

            if (!request.isScheduledForToday() && !request.isScheduledInPast()) {
                _events.tryEmit("Puoi contrassegnare come completata solo durante o dopo la data di svolgimento")
                return@launch
            }

            try {
                val updatedRequest = request.copy(completed = true)
                requestRepository.updateRequest(updatedRequest)

                // Aggiorna le missioni e XP per tutti i partecipanti
                updateMissionsForCompletion(request)

                _events.tryEmit("Richiesta contrassegnata come completata")
                //onCompleted()
            } catch (e: Exception) {
                _events.tryEmit("Errore nel completamento: ${e.message}")
            }
        }
    }

    // Aggiungi questo metodo per aggiornare le missioni quando si completa una richiesta
    private suspend fun updateMissionsForCompletion(request: Request) {
        try {
            val userId = settingsRepository.userIdFlow.first()
            if (userId <= 0) return

            // Ottieni i tag della richiesta come stringhe (nomi)
            val requestTags = tagsRepository.getTagsForRequest(request.id)
            val tagNames = requestTags.map { it.name }

            // Aggiorna le missioni per tutti gli utenti che hanno partecipato
            val allParticipants = request.rescuers

            for (participantId in allParticipants) {
                // Aggiorna le missioni per ogni partecipante
                missionRepository.updateMissionsForCompletedRequest(participantId, tagNames)

                // Aggiungi XP per ogni partecipante
                missionRepository.addExperienceForCompletedRequest(participantId, request.difficulty)
            }

            _events.tryEmit("Missioni e XP aggiornati per tutti i partecipanti!")
        } catch (e: Exception) {
            _events.tryEmit("Errore nell'aggiornamento delle missioni: ${e.message}")
        }
    }

    //metodo per gestire l'auto-completamento con aggiornamento missioni
    suspend fun processAutoCompletedRequests() {
        try {
            // Prima ottieni le richieste che stanno per essere auto-completate
            val currentTime = System.currentTimeMillis()
            val expiredRequests = requestRepository.getExpiredRequests(currentTime)

            // Per ogni richiesta scaduta, aggiorna le missioni prima di marcarla come completata
            for (request in expiredRequests) {
                if (!request.completed) {
                    updateMissionsForCompletion(request)
                }
            }

            // Poi marca tutte come completate
            requestRepository.markExpiredRequestsAsCompleted(currentTime)

        } catch (e: Exception) {
            _events.tryEmit("Errore nel processo di auto-completamento: ${e.message}")
        }
    }

    fun refreshRequest() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current is UiState.Ready) {
                loadParticipants(current.request)
            }
        }
    }
}