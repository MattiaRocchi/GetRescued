package com.example.myapplication.ui.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.database.RequestDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RequestsViewModel(private val requestDao: RequestDao) : ViewModel() {

    private val _requests = MutableStateFlow<List<Request>>(emptyList())
    val requests: StateFlow<List<Request>> = _requests

    init {
        viewModelScope.launch {
            requestDao.getOpenRequests()
                .collect { _requests.value = it }
        }
    }
}