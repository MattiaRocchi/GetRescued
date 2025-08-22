package com.example.myapplication.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.TitleBadge
import com.example.myapplication.data.database.UserWithInfo
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.TitleBadgeRepository
import com.example.myapplication.data.repositories.UserDaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(

    private val titleBadgeRepository: TitleBadgeRepository,
    private val userDaoRepository: UserDaoRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val userId = settingsRepository.userIdFlow
        .stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(5000), -1)
    private val _userTitles = MutableStateFlow<List<TitleBadge>>(emptyList())
    val userTitles: StateFlow<List<TitleBadge>> = _userTitles


    private val _userActiveTitle = MutableStateFlow<TitleBadge?>(null)
    val userActiveTitle: StateFlow<TitleBadge?> = _userActiveTitle

    private val _allTitles = MutableStateFlow<List<TitleBadge>>(emptyList())
    val allTitles: StateFlow<List<TitleBadge>> = _allTitles
    private val _user = MutableStateFlow<UserWithInfo?>(null)
    val user: StateFlow<UserWithInfo?> = _user

    init {
        viewModelScope.launch {
            settingsRepository.userIdFlow.collect { id ->
                if (id != -1) {
                    val data = userDaoRepository.getUserWithInfo(id)
                    _user.value = data
                }
            }
        }
    }

    fun updateProfilePhoto(newUri: String) {
        viewModelScope.launch {
            val userId = userId.first() // dal tuo SettingsRepository
            if (userId != -1) {
                val success = userDaoRepository.updateProfPic(userId, newUri)
                if (success) {
                    // refresh user data
                    _user.value = userDaoRepository.getUserWithInfo(userId)
                }
            }
        }
    }

    fun updateActiveTitle(newTitle: Int) {
        viewModelScope.launch {
            val userId = userId.first()
            if (userId != -1) {
                titleBadgeRepository.updateActiveTitle(userId, newTitle)
                // refresh user data
                _user.value = userDaoRepository.getUserWithInfo(userId)
            }
        }
    }

    fun getAllTitles() {
        viewModelScope.launch {
            val titles = titleBadgeRepository.getAll()
            _allTitles.value = titles
        }
    }
    fun getUserTitles() {
        viewModelScope.launch {
            val userId = userId.first()
            if (userId != -1) {
                val titles = titleBadgeRepository.getUserTitles(userId)
                _userTitles.value = titles
            }
        }
    }
    fun getActiveTitle() {
        viewModelScope.launch {
            val userId = userId.first()
            if (userId != -1) {
                val title = titleBadgeRepository.getActiveTitleByUserId(userId)
                _userActiveTitle.value = title
            } else {
                _userActiveTitle.value = null
            }
        }
    }





    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.logout()
            onSuccess()
        }
    }
}