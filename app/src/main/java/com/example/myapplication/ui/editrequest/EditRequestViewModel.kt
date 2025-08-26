package com.example.myapplication.ui.editrequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.database.TitleBadge
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.TitleBadgeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditRequestViewModel(
    private val repository: RequestDaoRepository,
    private val titleBadgeRepository: TitleBadgeRepository,
    private val requestId: Int // Ora iniettato da Koin con parametersOf
) : ViewModel() {

    val requestFlow: StateFlow<Request?> =
        repository.getRequestByIdFlow(requestId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _difficulty = MutableStateFlow("Bassa")
    val difficulty: StateFlow<String> = _difficulty

    private val _peopleRequired = MutableStateFlow(1)
    val peopleRequired: StateFlow<Int> = _peopleRequired

    private val _location = MutableStateFlow("")
    val location: StateFlow<String> = _location

    private val _images = MutableStateFlow<List<String>>(emptyList())
    val images: StateFlow<List<String>> = _images

    private val _selectedBadge = MutableStateFlow<TitleBadge?>(null)
    val selectedBadge: StateFlow<TitleBadge?> = _selectedBadge

    // Lista di tutti i badge disponibili
    val availableBadges: StateFlow<List<TitleBadge>> =
        titleBadgeRepository.getAllTitleBadges()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            requestFlow.filterNotNull().collect { r ->
                _title.value = r.title
                _description.value = r.description
                _difficulty.value = r.difficulty
                _peopleRequired.value = r.peopleRequired
                _location.value = r.place ?: ""
                _images.value = r.fotos
            }
        }
    }

    fun onTitleChange(v: String) { _title.value = v }
    fun onDescriptionChange(v: String) { _description.value = v }
    fun onDifficultyChange(v: String) { _difficulty.value = v }
    fun onPeopleRequiredChange(v: Int) { _peopleRequired.value = v }
    fun onLocationChange(v: String) { _location.value = v }

    fun addImage(imageUri: String) {
        _images.value = _images.value + imageUri
    }

    fun removeImage(imageUri: String) {
        _images.value = _images.value - imageUri
    }

    fun onBadgeSelected(badge: TitleBadge?) {
        _selectedBadge.value = badge
    }

    fun save(onDone: () -> Unit) {
        val current = requestFlow.value ?: return

        if (_title.value.isBlank()) {
            _events.tryEmit("Il titolo non può essere vuoto")
            return
        }

        if (_description.value.isBlank()) {
            _events.tryEmit("La descrizione non può essere vuota")
            return
        }

        if (_peopleRequired.value <= 0) {
            _events.tryEmit("Il numero di persone deve essere maggiore di 0")
            return
        }

        val updated = current.copy(
            title = title.value.trim(),
            description = description.value.trim(),
            difficulty = difficulty.value,
            peopleRequired = peopleRequired.value,
            place = if (location.value.isBlank()) null else location.value.trim(),
            //fotos = if (images.value.isEmpty()) null else images.value
        )

        viewModelScope.launch {
            try {
                repository.updateRequest(updated)
                _events.tryEmit("Richiesta aggiornata con successo")
                onDone()
            } catch (e: Exception) {
                _events.tryEmit("Errore nel salvare: ${e.message}")
            }
        }
    }
}