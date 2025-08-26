package com.example.myapplication.ui.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.repositories.RequestDaoRepository
import kotlinx.coroutines.flow.*

class RequestsViewModel(
    private val repository: RequestDaoRepository
) : ViewModel() {

    // Tutte le richieste (utile per debug o altre funzionalità)
    val requests: StateFlow<List<Request>> =
        repository.getAllRequests()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Richieste disponibili: non completate e con posti liberi
    val availableRequests: StateFlow<List<Request>> =
        repository.getOpenRequests() // Usa già getOpenRequests() che filtra per completed = 0
            .map { requests ->
                requests.filter { request ->
                    // Filtra solo le richieste che hanno ancora posti liberi
                    request.rescuers.size < request.peopleRequired
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}