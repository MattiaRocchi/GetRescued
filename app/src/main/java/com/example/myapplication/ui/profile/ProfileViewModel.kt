package com.example.myapplication.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.TitleBadgeRepository
import com.example.myapplication.data.repositories.UserDaoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(

    private val titleBadgeRepository: TitleBadgeRepository,
    private val userDaoRepository: UserDaoRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val userId = settingsRepository.userIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    val email = settingsRepository.emailFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val name = settingsRepository.nameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val surname = settingsRepository.surnameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val titleBadge = settingsRepository.activeTitleBadge
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.logout()
            onSuccess()
        }
    }
}