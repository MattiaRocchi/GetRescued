package com.example.myapplication.ui.inforequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class InfoRequestViewModel(
    private val requestRepository: RequestDaoRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val request: Request) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    private val _currentUserId = MutableStateFlow<Int?>(null)

    init {
        viewModelScope.launch {
            settingsRepository.userIdFlow.collect { userId ->
                _currentUserId.value = userId
            }

        }
    }

    fun loadRequest(requestId: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                println("DEBUG: Cerco richiesta ID: $requestId")

                val allRequests = requestRepository.getOpenRequests().first()
                println("DEBUG: Tutte le richieste: ${allRequests.map { it.id }}")

                val request = requestRepository.getRequestById(requestId)
                println("DEBUG: Richiesta trovata: $request")

                if (request != null) {
                    _uiState.value = UiState.Success(request)
                } else {
                    _uiState.value = UiState.Error("Richiesta non trovata. ID: $requestId")
                }
            } catch (e: Exception) {
                println("DEBUG: Errore: ${e.message}")
                _uiState.value = UiState.Error("Errore: ${e.message}")
            }
        }
    }

    fun participateInRequest(request: Request) {
        viewModelScope.launch {
            try {
                val currentUserId = _currentUserId.value ?: run {
                    _uiState.value = UiState.Error("Devi essere loggato per partecipare")
                    return@launch
                }

                if (currentUserId in request.rescuers) {
                    _uiState.value = UiState.Error("Hai già partecipato a questa richiesta")
                    return@launch
                }

                val updatedRequest = request.copy(
                    rescuers = request.rescuers + currentUserId
                )

                requestRepository.updateRequest(updatedRequest)
                // Il Flow si aggiornerà automaticamente con i nuovi dati

            } catch (e: Exception) {
                _uiState.value = UiState.Error("Errore: ${e.message}")
            }
        }
    }
}