package com.example.myapplication.ui.settingsScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repositories.SettingsRepository
import kotlinx.coroutines.launch


class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    // StateFlow/LiveData to expose to UI
    val musicEnabled = repository.musicEnabledFlow
    val musicVolume = repository.musicVolumeFlow
    val cameraEnabled = repository.cameraEnabledFlow
    val locationEnabled = repository.locationEnabledFlow

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setMusicEnabled(enabled) }
    }

    fun setMusicVolume(volume: Float) {
        viewModelScope.launch { repository.setMusicVolume(volume) }
    }

    fun setCameraEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setCameraEnabled(enabled) }
    }

    fun setLocationEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setLocationEnabled(enabled) }
    }
}
