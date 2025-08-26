package com.example.myapplication.ui.participationrequests

import androidx.lifecycle.ViewModel
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