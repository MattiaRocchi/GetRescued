package com.example.myapplication.ui.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.repositories.RequestDaoRepository
import kotlinx.coroutines.flow.*

class RequestsViewModel(
    private val repository: RequestDaoRepository
) : ViewModel() {

    val requests: StateFlow<List<Request>> =
        repository.getAllRequests()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class RequestsViewModelFactory(
    private val repository: RequestDaoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RequestsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RequestsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}