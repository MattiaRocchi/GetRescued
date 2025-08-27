package com.example.myapplication.ui.managerequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.database.UserWithInfo
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.UserDaoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ManageRequestViewModel(
    private val requestRepository: RequestDaoRepository,
    private val userDaoRepository: UserDaoRepository,
    private val settingsRepository: SettingsRepository,
    private val requestId: Int
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        object NotFound : UiState()
        object NotAuthorized : UiState()
        data class Ready(
            val request: Request,
            val pendingParticipants: List<UserWithInfo> = emptyList(),
            val approvedParticipants: List<UserWithInfo> = emptyList()
        ) : UiState()
    }

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val requestFlow: Flow<Request?> = requestRepository.getRequestByIdFlow(requestId)
    private val userIdFlow: Flow<Int> = settingsRepository.userIdFlow

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(requestFlow, userIdFlow) { req, uid ->
                when {
                    req == null -> UiState.NotFound
                    uid <= 0 || uid != req.sender -> UiState.NotAuthorized
                    else -> {
                        // Inizia con i dati base, poi caricheremo i partecipanti
                        UiState.Ready(
                            request = req,
                            pendingParticipants = emptyList(),
                            approvedParticipants = emptyList()
                        )
                    }
                }
            }
                .distinctUntilChanged()
                .collect { state ->
                    _uiState.value = state

                    // Se siamo nello stato Ready, carica i partecipanti
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

            _uiState.update { current ->
                if (current is UiState.Ready) {
                    current.copy(
                        approvedParticipants = approvedUsers,
                        pendingParticipants = pendingUsers
                    )
                } else current
            }
        } catch (e: Exception) {
            _events.tryEmit("Errore nel caricamento dei partecipanti: ${e.message}")
        }
    }

    fun approveParticipant(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _uiState.value
            if (current !is UiState.Ready) return@launch

            val request = current.request

            // Verifica se l'utente è già approvato
            if (userId in request.rescuers) {
                _events.tryEmit("Questo utente è già approvato")
                return@launch
            }

            // Verifica se ci sono ancora posti disponibili
            if (request.rescuers.size >= request.peopleRequired) {
                _events.tryEmit("Non ci sono più posti disponibili")
                return@launch
            }

            try {
                // Aggiungi l'utente alla lista dei rescuers
                val updatedRequest = request.copy(rescuers = request.rescuers + userId)
                requestRepository.updateRequest(updatedRequest)

                // Rimuovi dalla tabella pending requests
                requestRepository.deletePendingRequest(request.id, userId)

                _events.tryEmit("✅ Partecipante approvato con successo!")

            } catch (e: Exception) {
                _events.tryEmit("❌ Errore nell'approvazione: ${e.message}")
            }
        }
    }

    fun rejectParticipant(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _uiState.value
            if (current !is UiState.Ready) return@launch

            try {
                // Rimuovi dalla tabella pending requests
                requestRepository.deletePendingRequest(current.request.id, userId)

                _events.tryEmit("❌ Richiesta di partecipazione rifiutata")

            } catch (e: Exception) {
                _events.tryEmit("❌ Errore nel rifiuto: ${e.message}")
            }
        }
    }

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
                // Rimuovi l'utente dalla lista dei rescuers
                val updatedRequest = request.copy(rescuers = request.rescuers - userId)
                requestRepository.updateRequest(updatedRequest)

                _events.tryEmit("🚪 Partecipante rimosso dalla richiesta")

            } catch (e: Exception) {
                _events.tryEmit("❌ Errore nella rimozione: ${e.message}")
            }
        }
    }
}