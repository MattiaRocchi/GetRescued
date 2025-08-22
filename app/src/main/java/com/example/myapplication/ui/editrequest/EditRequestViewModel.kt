package com.example.myapplication.ui.editrequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.repositories.RequestDaoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditRequestViewModel(
    private val repository: RequestDaoRepository,
    private val requestId: Int
) : ViewModel() {

    // Request reattiva dal DB
    val requestFlow: StateFlow<Request?> =
        repository.getRequestByIdFlow(requestId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Campi di editing (si sincronizzano quando arriva la request)
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

    init {
        viewModelScope.launch {
            requestFlow.filterNotNull().collect { r ->
                _title.value = r.title
                _description.value = r.description
                _difficulty.value = r.difficulty
                _peopleRequired.value = r.peopleRequired
                _location.value = r.place ?: ""
            }
        }
    }

    fun onTitleChange(v: String) { _title.value = v }
    fun onDescriptionChange(v: String) { _description.value = v }
    fun onDifficultyChange(v: String) { _difficulty.value = v }
    fun onPeopleRequiredChange(v: Int) { _peopleRequired.value = v }
    fun onLocationChange(v: String) { _location.value = v }

    fun save(onDone: () -> Unit) {
        val current = requestFlow.value ?: return
        val updated = current.copy(
            title = title.value,
            description = description.value,
            difficulty = difficulty.value,
            peopleRequired = peopleRequired.value,
            place = location.value
        )
        viewModelScope.launch {
            repository.updateRequest(updated)
            onDone()
        }
    }
}

class EditRequestViewModelFactory(
    private val repository: RequestDaoRepository,
    private val requestId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditRequestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditRequestViewModel(repository, requestId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}