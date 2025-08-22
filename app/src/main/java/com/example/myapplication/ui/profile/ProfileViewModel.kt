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

    // UserId dallo store
    val userId = settingsRepository.userIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    // Stato UI
    private val _user = MutableStateFlow<UserWithInfo?>(null)
    val user: StateFlow<UserWithInfo?> = _user

    private val _userTitles = MutableStateFlow<List<TitleBadge>>(emptyList())
    val userTitles: StateFlow<List<TitleBadge>> = _userTitles

    private val _userActiveTitle = MutableStateFlow<TitleBadge?>(null)
    val userActiveTitle: StateFlow<TitleBadge?> = _userActiveTitle

    private val _allTitles = MutableStateFlow<List<TitleBadge>>(emptyList())
    val allTitles: StateFlow<List<TitleBadge>> = _allTitles

    init {
        // 🔹 ogni volta che cambia userId → ricarico tutto
        viewModelScope.launch {
            userId.collect { id ->
                if (id != -1) {
                    _user.value = userDaoRepository.getUserWithInfo(id)
                    _userTitles.value = titleBadgeRepository.getUserTitles(id)
                    _userActiveTitle.value = titleBadgeRepository.getActiveTitleByUserId(id)
                    _allTitles.value = titleBadgeRepository.getAll()
                } else {
                    // Reset se non loggato
                    _user.value = null
                    _userTitles.value = emptyList()
                    _userActiveTitle.value = null
                    _allTitles.value = emptyList()
                }
            }
        }
    }

    fun updateProfilePhoto(newUri: String) {
        viewModelScope.launch {
            val id = userId.value
            if (id != -1) {
                val success = userDaoRepository.updateProfPic(id, newUri)
                if (success) {
                    _user.value = userDaoRepository.getUserWithInfo(id) // refresh
                }
            }
        }
    }

    fun updateActiveTitle(newTitleId: Int) {
        viewModelScope.launch {
            val id = userId.value
            if (id != -1) {
                titleBadgeRepository.updateActiveTitle(id, newTitleId)
                _user.value = userDaoRepository.getUserWithInfo(id) // refresh user
                _userActiveTitle.value = titleBadgeRepository.getActiveTitleByUserId(id)
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
