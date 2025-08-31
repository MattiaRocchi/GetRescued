package com.example.myapplication.ui.editrequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.database.Tags
import com.example.myapplication.data.database.TagsRequest
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.TagsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class EditRequestViewModel(
    private val repository: RequestDaoRepository,
    private val tagsRepository: TagsRepository,
    private val requestId: Int
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

    // Campo per la data di svolgimento
    private val _scheduledDate = MutableStateFlow(LocalDate.now())
    val scheduledDate: StateFlow<LocalDate> = _scheduledDate

    private val _requiredTags = MutableStateFlow<List<Tags>>(emptyList())
    val requiredTags: StateFlow<List<Tags>> = _requiredTags

    // Lista di tutti i tag disponibili
    val availableTags: StateFlow<List<Tags>> =
        tagsRepository.allTagsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Tag attualmente associati alla richiesta
    val currentRequestTags: StateFlow<List<Tags>> =
        tagsRepository.getTagsForRequestFlow(requestId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        // Carica i dati della richiesta nei campi di modifica
        viewModelScope.launch {
            requestFlow.filterNotNull().collect { r ->
                _title.value = r.title
                _description.value = r.description
                _difficulty.value = r.difficulty
                _peopleRequired.value = r.peopleRequired
                _location.value = r.place ?: ""
                _images.value = r.fotos

                // Converti il timestamp in LocalDate
                val scheduledLocalDate = java.time.Instant.ofEpochMilli(r.scheduledDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                _scheduledDate.value = scheduledLocalDate
            }
        }

        // Carica i tag attualmente associati alla richiesta
        viewModelScope.launch {
            currentRequestTags.collect { tags ->
                _requiredTags.value = tags
            }
        }
    }

    fun onTitleChange(v: String) { _title.value = v }
    fun onDescriptionChange(v: String) { _description.value = v }
    fun onDifficultyChange(v: String) { _difficulty.value = v }
    fun onPeopleRequiredChange(v: Int) { _peopleRequired.value = v }
    fun onLocationChange(v: String) { _location.value = v }
    fun onScheduledDateChange(date: LocalDate) {
        // Assicuriamoci che la data non sia nel passato
        if (!date.isBefore(LocalDate.now())) {
            _scheduledDate.value = date
        }
    }

    fun addImage(imageUri: String) {
        val currentImages = _images.value.toMutableList()
        if (!currentImages.contains(imageUri)) {
            currentImages.add(imageUri)
            _images.value = currentImages
        }
    }

    fun removeImage(imageUri: String) {
        val currentImages = _images.value.toMutableList()
        currentImages.remove(imageUri)
        _images.value = currentImages
    }

    // Funzioni per gestire i tag
    fun addRequiredTag(tag: Tags) {
        val current = _requiredTags.value.toMutableList()
        if (!current.contains(tag)) {
            current.add(tag)
            _requiredTags.value = current
        }
    }

    fun removeRequiredTag(tag: Tags) {
        val current = _requiredTags.value.toMutableList()
        current.remove(tag)
        _requiredTags.value = current
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

        if (_scheduledDate.value.isBefore(LocalDate.now())) {
            _events.tryEmit("La data di svolgimento deve essere odierna o futura")
            return
        }

        // Converti LocalDate in timestamp
        val scheduledDateMillis = _scheduledDate.value
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val updated = current.copy(
            title = title.value.trim(),
            description = description.value.trim(),
            difficulty = difficulty.value,
            peopleRequired = peopleRequired.value,
            place = if (location.value.isBlank()) null else location.value.trim(),
            fotos = images.value,
            scheduledDate = scheduledDateMillis
        )

        viewModelScope.launch {
            try {
                // Aggiorna la richiesta
                repository.updateRequest(updated)

                // Aggiorna i tag associati alla richiesta
                tagsRepository.deleteTagsForRequest(requestId)

                val tagsToAdd = _requiredTags.value.map { tag ->
                    TagsRequest(
                        idTags = tag.id,
                        idRequest = requestId
                    )
                }

                if (tagsToAdd.isNotEmpty()) {
                    tagsRepository.insertTagsForRequest(*tagsToAdd.toTypedArray())
                }

                _events.tryEmit("Richiesta aggiornata con successo")
                onDone()
            } catch (e: Exception) {
                _events.tryEmit("Errore nel salvare: ${e.message}")
            }
        }
    }
}