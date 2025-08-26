package com.example.myapplication.ui.inforequest

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

class InfoRequestViewModel(
    private val requestRepository: RequestDaoRepository,
    private val userDaoRepository: UserDaoRepository,
    private val settingsRepository: SettingsRepository,
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
            val isFull: Boolean = false
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
                if (req == null) {
                    UiState.NotFound
                } else {
                    val isCreator = (uid != -1 && uid == req.sender)
                    val isParticipating = (uid != -1 && uid in req.rescuers)
                    val isFull = req.rescuers.size >= req.peopleRequired
                    UiState.Ready(
                        request = req,
                        creator = null,
                        isCreator = isCreator,
                        isParticipating = isParticipating,
                        isFull = isFull
                    )
                }
            }
                .distinctUntilChanged()
                .collect { s -> _uiState.value = s }
        }

        viewModelScope.launch {
            requestFlow
                .filterNotNull()
                .collect { req ->
                    launch(Dispatchers.IO) {
                        try {
                            val creator: UserWithInfo? = userDaoRepository.getUserWithInfo(req.sender)
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
    }

    fun participate() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current !is UiState.Ready) {
                _events.tryEmit("Richiesta non pronta")
                return@launch
            }

            val uid = userIdFlow.first()
            if (uid == -1) {
                _events.emit("Devi effettuare il login per partecipare")
                return@launch
            }

            val req = current.request
            if (uid == req.sender) {
                _events.emit("Sei il creatore della richiesta")
                return@launch
            }
            if (uid in req.rescuers) {
                _events.emit("Hai già partecipato")
                return@launch
            }
            if (req.rescuers.size >= req.peopleRequired) {
                _events.emit("Posti esauriti")
                return@launch
            }

            try {
                val updated = req.copy(rescuers = req.rescuers + uid)
                requestRepository.updateRequest(updated)
                _events.emit("Partecipazione registrata")
            } catch (t: Throwable) {
                _events.emit("Errore durante la partecipazione: ${t.message ?: "sconosciuto"}")
            }
        }
    }
}