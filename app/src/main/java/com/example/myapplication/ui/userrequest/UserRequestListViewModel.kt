package com.example.myapplication.ui.userrequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import kotlinx.coroutines.flow.*

class UserRequestListViewModel(
    private val repository: RequestDaoRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val userIdFlow: Flow<Int> =
        settingsRepository.userIdFlow.filter { it != -1 }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val myRequests: StateFlow<List<Request>> =
        userIdFlow
            .flatMapLatest { uid -> repository.getRequestsByUser(uid) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}