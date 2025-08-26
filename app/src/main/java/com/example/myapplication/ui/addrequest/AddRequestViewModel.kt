package com.example.myapplication.ui.addrequest

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.database.TitleBadge
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.TitleBadgeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AddRequestViewModel(
    private val repository: RequestDaoRepository,
    private val settingsRepository: SettingsRepository,
    private val titleBadgeRepository: TitleBadgeRepository
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

    private val _requiredBadges = MutableStateFlow<List<TitleBadge>>(emptyList())
    val requiredBadges: StateFlow<List<TitleBadge>> = _requiredBadges.asStateFlow()

    private val _availableBadges = MutableStateFlow<List<TitleBadge>>(emptyList())
    val availableBadges: StateFlow<List<TitleBadge>> = _availableBadges.asStateFlow()

    private val _isFormValid = MutableStateFlow(false)
    val isFormValid: StateFlow<Boolean> = _isFormValid.asStateFlow()

    init {
        // Carica tutti i badge disponibili
        viewModelScope.launch {
            try {
                val badges = titleBadgeRepository.getAll()
                _availableBadges.value = badges
            } catch (e: Exception) {
                // Handle error
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

    fun addRequiredBadge(badge: TitleBadge) {
        val current = _requiredBadges.value.toMutableList()
        if (!current.contains(badge)) {
            current.add(badge)
            _requiredBadges.value = current
        }
    }

    fun removeRequiredBadge(badge: TitleBadge) {
        val current = _requiredBadges.value.toMutableList()
        current.remove(badge)
        _requiredBadges.value = current
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
                repository.insertRequest(request)
                // TODO: Collegare i badge richiesti usando TagsMission

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
        _requiredBadges.value = emptyList()
    }
}