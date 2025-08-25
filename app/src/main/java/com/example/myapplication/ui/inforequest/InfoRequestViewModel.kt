package com.example.myapplication.ui.inforequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.database.UserWithInfo
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.UserDaoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel per la schermata di dettaglio richiesta.
 *
 * - Non blocca la visualizzazione della richiesta in attesa di userId/creatorName.
 * - Mostra la request non appena Room emette la riga.
 * - Arricchisce con creatorName in modo asincrono.
 * - Espone eventi one-shot tramite SharedFlow (snackbar).
 */
class InfoRequestViewModel(
    private val requestRepository: RequestDaoRepository,
    private val userDaoRepository: UserDaoRepository,
    private val settingsRepository: SettingsRepository,
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
            val isFull: Boolean = false
        ) : UiState()
    }

    // eventi one-shot (snackbars)
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    // Flow diretto dalla richiesta (Room)
    private val requestFlow: Flow<Request?> = requestRepository.getRequestByIdFlow(requestId)

    // userIdFlow (emette anche -1 se non loggato). NON filtrare per evitare blocchi.
    private val userIdFlow: Flow<Int> = settingsRepository.userIdFlow

    // Stato UI: inizialmente Loading
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // 1) Combina requestFlow + userIdFlow per avere subito uno stato Ready minimale (creator=null)
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
                .distinctUntilChanged() // piccolo ottimizzazione
                .collect { s -> _uiState.value = s }
        }

        // 2) Ogni volta che arriva una request valida, carica (async) il creator (UserWithInfo)
        viewModelScope.launch {
            requestFlow
                .filterNotNull()
                .collect { req ->
                    // carichiamo il creator con repository (suspend) su IO
                    launch(Dispatchers.IO) {
                        try {
                            val creator: UserWithInfo? = userDaoRepository.getUserWithInfo(req.sender)
                            // aggiorna stato solo se la request corrente è la stessa (evitiamo race)
                            _uiState.update { current ->
                                when (current) {
                                    is UiState.Ready -> {
                                        // se la request è ancora la stessa id, aggiorna creator
                                        if (current.request.id == req.id) {
                                            current.copy(creator = creator)
                                        } else current
                                    }
                                    else -> current
                                }
                            }
                        } catch (t: Throwable) {
                            // non blocchiamo la UI per un errore nel caricamento del creator
                            _events.tryEmit("Impossibile caricare informazioni creatore")
                        }
                    }
                }
        }

        // 3) Se la request viene aggiornata (es. partecipazione), vogliamo ricomputare flags;
        //    userIdFlow + requestFlow combinati sopra si occupano di questo, quindi niente altro qui.
    }

    /**
     * Tenta di partecipare alla richiesta:
     *  - controlla utente valido;
     *  - evita duplicati;
     *  - aggiorna DB via repository.
     */
    fun participate() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current !is UiState.Ready) {
                _events.tryEmit("Richiesta non pronta")
                return@launch
            }

            // prendi userId attuale (sospende finché non emette il primo valore)
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
                // Room emetterà la request aggiornata -> combine sopra ricalcolerà stato
            } catch (t: Throwable) {
                _events.emit("Errore durante la partecipazione: ${t.message ?: "sconosciuto"}")
            }
        }
    }
}

/**
 * Factory manuale per InfoRequestViewModel (senza Hilt)
 */
class InfoRequestViewModelFactory(
    private val requestRepository: RequestDaoRepository,
    private val userDaoRepository: UserDaoRepository,
    private val settingsRepository: SettingsRepository,
    private val requestId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InfoRequestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InfoRequestViewModel(requestRepository, userDaoRepository, settingsRepository, requestId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}