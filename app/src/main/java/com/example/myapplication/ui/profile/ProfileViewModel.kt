package com.example.myapplication.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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


    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.logout()
            onSuccess()
        }
    }
}