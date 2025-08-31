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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log

class InfoRequestViewModel(
    private val requestRepository: RequestDaoRepository,
    private val userDaoRepository: UserDaoRepository,
    private val settingsRepository: SettingsRepository,
    private val tagsRepository: TagsRepository,
    private val requestId: Int
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

    // StateFlow per i tags della richiesta
    private val _requestTags = MutableStateFlow<List<Tags>>(emptyList())
    val requestTags: StateFlow<List<Tags>> = _requestTags.asStateFlow()

    init {
        // Carica tutto insieme aspettando che il creator sia disponibile
        viewModelScope.launch {
            requestFlow.collect { request ->
                if (request == null) {
                    _uiState.value = UiState.NotFound
                } else {
                    try {
                        // Carica il creator prima di emettere Ready
                        val creator = userDaoRepository.getUserWithInfo(request.sender)
                        val userId = userIdFlow.first()

                        val isValidUser = userId > 0
                        val isCreator = isValidUser && userId == request.sender
                        val isParticipating = isValidUser && userId in request.rescuers
                        val isFull = request.rescuers.size >= request.peopleRequired

                        // Controlla se l'utente ha una richiesta pending
                        val isPending = if (isValidUser && !isCreator && !isParticipating) {
                            try {
                                val pendingRequests = requestRepository.getPendingRequestsForRequest(request.id)
                                pendingRequests.any { it.userId == userId }
                            } catch (e: Exception) {
                                false
                            }
                        } else false

                        _uiState.value = UiState.Ready(
                            request = request,
                            creator = creator,
                            isCreator = isCreator,
                            isParticipating = isParticipating,
                            isPending = isPending,
                            isFull = isFull
                        )

                        // Aggiorna canParticipate ogni volta che cambia lo stato
                        updateCanParticipate(userId, isCreator, isParticipating, isPending, isFull)

                    } catch (e: Exception) {
                        // Se fallisce il caricamento del creator, emette Ready con creator = null
                        val userId = userIdFlow.first()
                        val isValidUser = userId > 0
                        val isCreator = isValidUser && userId == request.sender
                        val isParticipating = isValidUser && userId in request.rescuers
                        val isFull = request.rescuers.size >= request.peopleRequired

                        val isPending = if (isValidUser && !isCreator && !isParticipating) {
                            try {
                                val pendingRequests = requestRepository.getPendingRequestsForRequest(request.id)
                                pendingRequests.any { it.userId == userId }
                            } catch (ex: Exception) {
                                false
                            }
                        } else false

                        _uiState.value = UiState.Ready(
                            request = request,
                            creator = null,
                            isCreator = isCreator,
                            isParticipating = isParticipating,
                            isPending = isPending,
                            isFull = isFull
                        )

                        // Aggiorna canParticipate anche in caso di errore
                        updateCanParticipate(userId, isCreator, isParticipating, isPending, isFull)

                        _events.tryEmit("Errore nel caricamento delle informazioni del creatore")
                    }
                }
            }
        }

        // Carica i tags della richiesta e li mantiene aggiornati
        viewModelScope.launch {
            try {
                val tags = tagsRepository.getTagsForRequest(requestId)
                _requestTags.value = tags
                Log.d("InfoRequestVM", "Loaded request tags: ${tags.map { it.name }}")
            } catch (e: Exception) {
                Log.e("InfoRequestVM", "Error loading request tags", e)
                _events.tryEmit("Errore nel caricamento dei tags")
                _requestTags.value = emptyList()
            }
        }
    }

    // Funzione separata per aggiornare canParticipate
    private suspend fun updateCanParticipate(
        userId: Int,
        isCreator: Boolean,
        isParticipating: Boolean,
        isPending: Boolean,
        isFull: Boolean
    ) {
        try {
            Log.d("InfoRequestVM", "Updating canParticipate for user $userId")

            // Se non è un utente valido, non può partecipare
            if (userId <= 0) {
                _canParticipate.value = false
                Log.d("InfoRequestVM", "Invalid user ID, canParticipate = false")
                return
            }

            // Se è il creatore, sta già partecipando, ha pending, o è pieno, non può partecipare
            if (isCreator || isParticipating || isPending || isFull) {
                _canParticipate.value = false
                Log.d("InfoRequestVM", "User cannot participate: creator=$isCreator, participating=$isParticipating, pending=$isPending, full=$isFull")
                return
            }

            // Controlla se ha i tag richiesti
            val hasRequiredTags = tagsRepository.userHasRequiredTags(userId, requestId)
            _canParticipate.value = hasRequiredTags

            Log.d("InfoRequestVM", "User has required tags: $hasRequiredTags")

            // Debug: mostra i tag dell'utente e quelli richiesti
            if (!hasRequiredTags) {
                try {
                    val userTags = tagsRepository.getTagsForUser(userId)
                    val requiredTags = _requestTags.value
                    Log.d("InfoRequestVM", "User tags: ${userTags.map { it.name }}")
                    Log.d("InfoRequestVM", "Required tags: ${requiredTags.map { it.name }}")
                } catch (e: Exception) {
                    Log.e("InfoRequestVM", "Error getting tags for debugging", e)
                }
            }

        } catch (e: Exception) {
            Log.e("InfoRequestVM", "Error updating canParticipate", e)
            _canParticipate.value = false
        }
    }

    fun refreshRequest() {
        viewModelScope.launch {
            try {
                // Ricarica i tag della richiesta
                val tags = tagsRepository.getTagsForRequest(requestId)
                _requestTags.value = tags

                // Forza un refresh dello stato corrente
                val currentState = _uiState.value
                if (currentState is UiState.Ready) {
                    val userId = userIdFlow.first()
                    updateCanParticipate(
                        userId = userId,
                        isCreator = currentState.isCreator,
                        isParticipating = currentState.isParticipating,
                        isPending = currentState.isPending,
                        isFull = currentState.isFull
                    )
                }

                Log.d("InfoRequestVM", "Request refreshed successfully")
            } catch (e: Exception) {
                Log.e("InfoRequestVM", "Error in refreshRequest", e)
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

            // Verifica nuovamente i tag prima di procedere
            try {
                val hasRequiredTags = tagsRepository.userHasRequiredTags(uid, requestId)
                if (!hasRequiredTags) {
                    _events.emit("Non hai tutti i tag richiesti per questa richiesta")
                    return@launch
                }
            } catch (e: Exception) {
                Log.e("InfoRequestVM", "Error checking tags in participate", e)
                _events.emit("Errore nella verifica dei requisiti")
                return@launch
            }

            try {
                val pendingRequest = PendingRequest(requestId = req.id, userId = uid)
                requestRepository.insertPendingRequest(pendingRequest)
                _events.emit("Richiesta di partecipazione inviata! Attendi l'approvazione del creatore.")

            } catch (t: Throwable) {
                if (t.message?.contains("UNIQUE constraint failed") == true) {
                    _events.emit("Hai già inviato una richiesta di partecipazione per questa richiesta")
                } else {
                    _events.emit("Errore durante l'invio della richiesta: ${t.message ?: "sconosciuto"}")
                }
            }

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
                _events.emit("Ti sei tirato indietro dalla richiesta")

                refreshRequest()

            } catch (t: Throwable) {
                _events.emit("Errore nel ritirarsi dalla richiesta: ${t.message ?: "sconosciuto"}")
            }
        }
    }
}