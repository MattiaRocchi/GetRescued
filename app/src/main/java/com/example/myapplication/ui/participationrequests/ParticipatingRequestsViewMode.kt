package com.example.myapplication.ui.participationrequests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import kotlinx.coroutines.flow.*

class ParticipatingRequestsViewModel(
    private val requestRepository: RequestDaoRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val userIdFlow: Flow<Int> =
        settingsRepository.validUserFlow.filterNotNull()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val participation: StateFlow<List<Request>> =
        userIdFlow
            .flatMapLatest { uid -> requestRepository.getRequestsParticipatingByUser(uid) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class ParticipatingRequestsViewModelFactory(
    private val requestRepository: RequestDaoRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ParticipatingRequestsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ParticipatingRequestsViewModel(requestRepository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}