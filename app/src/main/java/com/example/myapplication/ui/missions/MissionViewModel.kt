package com.example.myapplication.ui.missions

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.repositories.MissionRepository
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.TitleBadgeRepository
import com.example.myapplication.data.repositories.UserDaoRepository

class MissionViewModel(
    private val titleBadgeRepository: TitleBadgeRepository,
    private val userDaoRepository: UserDaoRepository,
    private val missionRepository: MissionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

}