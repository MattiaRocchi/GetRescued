package com.example.myapplication.ui.userrequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import kotlinx.coroutines.flow.*

class UserRequestListViewModel(
    private val repository: RequestDaoRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // userId letto dal SettingsRepository
    private val userIdFlow: Flow<Int> =
        settingsRepository.userIdFlow.filterNotNull()

    // Lista richieste create dall'utente corrente
    val myRequests: StateFlow<List<Request>> =
        userIdFlow
            .flatMapLatest { uid -> repository.getRequestsByUser(uid) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class UserRequestListViewModelFactory(
    private val repository: RequestDaoRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserRequestListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserRequestListViewModel(repository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
