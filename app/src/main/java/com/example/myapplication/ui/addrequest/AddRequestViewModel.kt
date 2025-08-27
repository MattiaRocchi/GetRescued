package com.example.myapplication.ui.addrequest

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.database.Tags
import com.example.myapplication.data.database.TagsMission
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.TagsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
                _peopleRequired
            ) { title, desc, loc, people ->
                title.isNotBlank() && desc.isNotBlank() && loc.isNotBlank() && people > 0
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

            val request = Request(
                title = title.value,
                sender = userId,
                rescuers = emptyList(),
                difficulty = difficulty.value,
                peopleRequired = peopleRequired.value,
                fotos = photos.value,
                description = description.value,
                place = location.value
            )

            try {
                // Inserisci la richiesta
                repository.insertRequest(request)

                // Se ci sono tag richiesti, dobbiamo collegarli alla richiesta
                // Nota: per fare questo correttamente, dovremmo avere l'ID della richiesta appena inserita
                // Il DAO insert dovrebbe restituire l'ID, ma attualmente non lo fa
                // Per ora, i tag richiesti non vengono salvati - questo richiede una modifica al DAO

                // TODO: Implementare il collegamento dei tag alla richiesta tramite TagsMission
                // Questo richiede che RequestDao.insert restituisca l'ID della richiesta inserita

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
    }
}