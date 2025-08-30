package com.example.myapplication.ui.inforequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.PendingRequest
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.database.Tags
import com.example.myapplication.data.database.UserWithInfo
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.TagsRepository
import com.example.myapplication.data.repositories.UserDaoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InfoRequestViewModel(
    private val requestRepository: RequestDaoRepository,
    private val userDaoRepository: UserDaoRepository,
    private val settingsRepository: SettingsRepository,
    private val tagsRepository: TagsRepository,
    private val requestId: Int // Ora iniettato da Koin con parametersOf
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        object NotFound : UiState()
        data class Ready(
            val request: Request,
            val creator: UserWithInfo? = null,
            val isCreator: Boolean = false,
            val isParticipating: Boolean = false,
            val isPending: Boolean = false,
            val isFull: Boolean = false
        ) : UiState()
    }

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val requestFlow: Flow<Request?> = requestRepository.getRequestByIdFlow(requestId)
    private val userIdFlow: Flow<Int> = settingsRepository.userIdFlow
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)

    private val _canParticipate = MutableStateFlow(false)
    val canParticipate: StateFlow<Boolean> = _canParticipate.asStateFlow()
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // NUOVO: StateFlow per i tags della richiesta
    private val _requestTags = MutableStateFlow<List<Tags>>(emptyList())
    val requestTags: StateFlow<List<Tags>> = _requestTags.asStateFlow()

    init {
        viewModelScope.launch {
            combine(requestFlow, userIdFlow) { req, uid ->
                if (req == null) {
                    UiState.NotFound
                } else {
                    val isValidUser = uid > 0
                    val isCreator = isValidUser && uid == req.sender
                    val isParticipating = isValidUser && uid in req.rescuers
                    val isFull = req.rescuers.size >= req.peopleRequired

                    // Controlla se l'utente ha una richiesta pending
                    val isPending = if (isValidUser && !isCreator && !isParticipating) {
                        try {
                            val pendingRequests =
                                requestRepository.getPendingRequestsForRequest(req.id)
                            pendingRequests.any { it.userId == uid }
                        } catch (e: Exception) {
                            false
                        }
                    } else false

                    UiState.Ready(
                        request = req,
                        creator = null,
                        isCreator = isCreator,
                        isParticipating = isParticipating,
                        isPending = isPending,
                        isFull = isFull
                    )
                }
            }
                .distinctUntilChanged()
                .collect { s -> _uiState.value = s }
        }

        // Carica informazioni del creatore
        viewModelScope.launch {
            requestFlow
                .filterNotNull()
                .collect { req ->
                    launch(Dispatchers.IO) {
                        try {
                            val creator: UserWithInfo? =
                                userDaoRepository.getUserWithInfo(req.sender)
                            _uiState.update { current ->
                                when (current) {
                                    is UiState.Ready -> {
                                        if (current.request.id == req.id) {
                                            current.copy(creator = creator)
                                        } else current
                                    }

                                    else -> current
                                }
                            }
                        } catch (t: Throwable) {
                            _events.tryEmit("Impossibile caricare informazioni creatore")
                        }
                    }
                }
        }

        // NUOVO: Carica i tags della richiesta
        viewModelScope.launch {
            try {
                val tags = tagsRepository.getTagsForRequest(requestId)
                _requestTags.value = tags
            } catch (e: Exception) {
                _events.tryEmit("Errore nel caricamento dei tags")
                _requestTags.value = emptyList()
            }
        }

        viewModelScope.launch {
            try {
                val userId = userIdFlow.first()
                if (userId > 0) {
                    val hasRequiredTags = tagsRepository.userHasRequiredTags(userId, requestId)
                    _canParticipate.value = hasRequiredTags
                }
            } catch (e: Exception) {
                _canParticipate.value = false
            }
        }
    }

    fun refreshRequest() {
        viewModelScope.launch {
            try {
                // Ricarica i tag della richiesta
                val tags = tagsRepository.getTagsForRequest(requestId)
                _requestTags.value = tags

                // Ricarica il controllo dei tag per l'utente
                val userId = userIdFlow.first()
                if (userId > 0) {
                    val hasRequiredTags = tagsRepository.userHasRequiredTags(userId, requestId)
                    _canParticipate.value = hasRequiredTags
                }
            } catch (e: Exception) {
                _events.tryEmit("Errore nel refresh: ${e.message}")
            }
        }
    }

    fun participate() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current !is UiState.Ready) {
                _events.tryEmit("Richiesta non pronta")
                return@launch
            }

            val uid = userIdFlow.first()
            if (uid <= 0) {
                _events.emit("Devi effettuare il login per partecipare")
                return@launch
            }

            val req = current.request
            if (uid == req.sender) {
                _events.emit("Sei il creatore della richiesta")
                return@launch
            }
            if (uid in req.rescuers) {
                _events.emit("Sei già stato approvato per questa richiesta")
                return@launch
            }

            try {
                val pendingRequest = PendingRequest(requestId = req.id, userId = uid)
                requestRepository.insertPendingRequest(pendingRequest)
                _events.emit("✅ Richiesta di partecipazione inviata! Attendi l'approvazione del creatore.")

            } catch (t: Throwable) {
                if (t.message?.contains("UNIQUE constraint failed") == true) {
                    _events.emit("⚠️ Hai già inviato una richiesta di partecipazione per questa richiesta")
                } else {
                    _events.emit("❌ Errore durante l'invio della richiesta: ${t.message ?: "sconosciuto"}")
                }
            }

            // REFRESH DOPO IL SUCCESSO
            refreshRequest()
        }
    }

    fun leaveRequest() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current !is UiState.Ready) {
                _events.tryEmit("Richiesta non pronta")
                return@launch
            }

            val uid = userIdFlow.first()
            if (uid <= 0) {
                _events.emit("Devi effettuare il login")
                return@launch
            }

            val req = current.request
            if (uid == req.sender) {
                _events.emit("Non puoi abbandonare una richiesta che hai creato tu")
                return@launch
            }
            if (uid !in req.rescuers) {
                _events.emit("Non stavi partecipando a questa richiesta")
                return@launch
            }

            try {
                val updated = req.copy(rescuers = req.rescuers - uid)
                requestRepository.updateRequest(updated)
                _events.emit("🚪 Ti sei tirato indietro dalla richiesta")

                // REFRESH DOPO IL SUCCESSO
                refreshRequest()

            } catch (t: Throwable) {
                _events.emit("❌ Errore nel ritirarsi dalla richiesta: ${t.message ?: "sconosciuto"}")
            }
        }
    }
}