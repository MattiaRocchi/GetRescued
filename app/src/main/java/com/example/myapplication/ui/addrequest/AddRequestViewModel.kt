package com.example.myapplication.ui.addrequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AddRequestViewModel(
    private val repository: RequestDaoRepository,
    private val settingsRepository: SettingsRepository
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

    fun onTitleChange(v: String) { _title.value = v }
    fun onDescriptionChange(v: String) { _description.value = v }
    fun onLocationChange(v: String) { _location.value = v }
    fun onPeopleRequiredChange(v: Int) { _peopleRequired.value = v }
    fun onDifficultyChange(v: String) { _difficulty.value = v }

    fun submitRequest(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val userId = settingsRepository.userIdFlow.firstOrNull()
                ?: run {
                    // qui potresti emettere un evento/snackbar
                    return@launch
                }

            val request = Request(
                title = title.value,
                sender = userId,
                rescuers = emptyList(),
                difficulty = difficulty.value,
                peopleRequired = peopleRequired.value,
                fotos = emptyList(),
                description = description.value,
                place = location.value
            )

            repository.insertRequest(request)

            // reset (opzionale)
            _title.value = ""
            _description.value = ""
            _peopleRequired.value = 1
            _difficulty.value = "Bassa"
            _location.value = ""

            onSuccess()
        }
    }
}

class AddRequestViewModelFactory(
    private val repository: RequestDaoRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddRequestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddRequestViewModel(repository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}