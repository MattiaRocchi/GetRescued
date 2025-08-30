package com.example.myapplication.ui.addrequest

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.database.Tags
import com.example.myapplication.data.database.TagsMission
import com.example.myapplication.data.database.TagsRequest
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.TagsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.absoluteValue

class AddRequestViewModel(
    private val repository: RequestDaoRepository,
    private val settingsRepository: SettingsRepository,
    private val tagsRepository: TagsRepository
) : ViewModel() {

    private val _location = MutableStateFlow("")
    val location: StateFlow<String> = _location.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _peopleRequired = MutableStateFlow(1)
    val peopleRequired: StateFlow<Int> = _peopleRequired

    private val _difficulty = MutableStateFlow("Bassa")
    val difficulty: StateFlow<String> = _difficulty

    private val _photos = MutableStateFlow<List<String>>(emptyList())
    val photos: StateFlow<List<String>> = _photos.asStateFlow()

    // Nuovo campo per la data di svolgimento
    private val _scheduledDate = MutableStateFlow(LocalDate.now())
    val scheduledDate: StateFlow<LocalDate> = _scheduledDate.asStateFlow()

    // Cambiato da TitleBadge a Tags
    private val _requiredTags = MutableStateFlow<List<Tags>>(emptyList())
    val requiredTags: StateFlow<List<Tags>> = _requiredTags.asStateFlow()

    private val _availableTags = MutableStateFlow<List<Tags>>(emptyList())
    val availableTags: StateFlow<List<Tags>> = _availableTags.asStateFlow()

    private val _isFormValid = MutableStateFlow(false)
    val isFormValid: StateFlow<Boolean> = _isFormValid.asStateFlow()

    init {
        // Carica tutti i tag disponibili
        viewModelScope.launch {
            tagsRepository.allTagsFlow().collect { tags ->
                _availableTags.value = tags
            }
        }

        // Valida il form ogni volta che cambiano i dati
        viewModelScope.launch {
            combine(
                _title,
                _description,
                _location,
                _peopleRequired,
                _scheduledDate
            ) { title, desc, loc, people, date ->
                title.isNotBlank() &&
                        desc.isNotBlank() &&
                        loc.isNotBlank() &&
                        people > 0 &&
                        !date.isBefore(LocalDate.now()) // La data deve essere oggi o futura
            }.collect { isValid ->
                _isFormValid.value = isValid
            }
        }
    }

    fun onTitleChange(v: String) { _title.value = v }
    fun onDescriptionChange(v: String) { _description.value = v }
    fun onLocationChange(v: String) { _location.value = v }
    fun onPeopleRequiredChange(v: Int) { _peopleRequired.value = maxOf(1, v) }
    fun onDifficultyChange(v: String) { _difficulty.value = v }
    fun onScheduledDateChange(date: LocalDate) {
        // Assicuriamoci che la data non sia nel passato
        if (!date.isBefore(LocalDate.now())) {
            _scheduledDate.value = date
        }
    }

    fun addPhoto(photoUri: Uri) {
        val currentPhotos = _photos.value.toMutableList()
        currentPhotos.add(photoUri.toString())
        _photos.value = currentPhotos
    }

    fun removePhoto(index: Int) {
        val currentPhotos = _photos.value.toMutableList()
        if (index in currentPhotos.indices) {
            currentPhotos.removeAt(index)
            _photos.value = currentPhotos
        }
    }

    // Cambiato per gestire Tags invece di TitleBadge
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

    fun submitRequest(onSuccess: () -> Unit) {
        if (!_isFormValid.value) return

        viewModelScope.launch {
            val userId = settingsRepository.userIdFlow.firstOrNull() ?: return@launch

            // Converti LocalDate in timestamp (inizio giornata)
            val scheduledDateMillis = scheduledDate.value
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val request = Request(
                title = title.value,
                sender = userId,
                rescuers = emptyList(),
                difficulty = difficulty.value,
                peopleRequired = peopleRequired.value,
                fotos = photos.value,
                description = description.value,
                place = location.value,
                scheduledDate = scheduledDateMillis
            )

            try {
                // Inserisci la richiesta e ottieni l'ID
                val requestId = repository.insertRequest(request)

                // Se ci sono tag richiesti, collegali alla richiesta
                if (_requiredTags.value.isNotEmpty()) {
                    val tagsToAdd = _requiredTags.value.map { tag ->
                        TagsRequest(
                            idTags = tag.id,
                            idRequest = requestId.hashCode()
                        )
                    }
                    tagsRepository.insertTagsForRequest(*tagsToAdd.toTypedArray())
                }

                // Reset form
                resetForm()
                onSuccess()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun resetForm() {
        _title.value = ""
        _description.value = ""
        _peopleRequired.value = 1
        _difficulty.value = "Bassa"
        _location.value = ""
        _photos.value = emptyList()
        _requiredTags.value = emptyList()
        _scheduledDate.value = LocalDate.now()
    }
}