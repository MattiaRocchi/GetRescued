package com.example.myapplication.ui.addrequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.database.RequestDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddRequestViewModel(private val requestDao: RequestDao) : ViewModel() {

    private val _location = MutableStateFlow("")
    val location: StateFlow<String> = _location.asStateFlow()

    // Stato per i campi del form
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _peopleRequired = MutableStateFlow(1)
    val peopleRequired: StateFlow<Int> = _peopleRequired

    private val _difficulty = MutableStateFlow("Bassa")
    val difficulty: StateFlow<String> = _difficulty

    fun onTitleChange(newValue: String) {
        _title.value = newValue
    }

    fun onDescriptionChange(newValue: String) {
        _description.value = newValue
    }

    fun onLocationChange(newLocation: String) {
        _location.value = newLocation
    }

    fun onPeopleRequiredChange(newValue: Int) {
        _peopleRequired.value = newValue
    }

    fun onDifficultyChange(newValue: String) {
        _difficulty.value = newValue
    }

    fun submitRequest(userId: Int, onSuccess: () -> Unit) {
        val request = Request(
            title = title.value,
            sender = userId,
            rescuers = listOf(),
            difficulty = difficulty.value,
            peopleRequired = peopleRequired.value,
            fotos = listOf(), // aggiungerai in futuro
            description = description.value,
            place = location.value
        )

        viewModelScope.launch {
            requestDao.insert(request)
            onSuccess()
        }
    }
}