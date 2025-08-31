package com.example.myapplication.ui.userrequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.PendingRequest
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.database.Tags
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.TagsRepository
import kotlinx.coroutines.flow.*

class UserRequestListViewModel(
    private val repository: RequestDaoRepository,
    private val settingsRepository: SettingsRepository,
    private val tagsRepository: TagsRepository
) : ViewModel() {

    private val userIdFlow: Flow<Int> =
        settingsRepository.userIdFlow.filter { it != -1 }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val myRequests: StateFlow<List<Request>> =
        userIdFlow
            .flatMapLatest { uid -> repository.getRequestsByUser(uid) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    //Funzioni suspend per ottenere i dati extra
    suspend fun getTagsForRequest(requestId: Int): List<Tags> {
        return tagsRepository.getTagsForRequest(requestId)
    }

    suspend fun getPendingRequestsForRequest(requestId: Int): List<PendingRequest> {
        return repository.getPendingRequestsForRequest(requestId)
    }
}