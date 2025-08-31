package com.example.myapplication.data.database

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class UserWithInfo(
    val id: Int,
    val name: String,
    val surname: String,
    val email: String,
    val age: Int,
    val habitation: String?,
    val phoneNumber: String?,
    val createdAt: Long,

    val activeTitle: Int,
    val exp: Int,
    val profileFoto: String?
)

data class MissionUiState(
    val isLoading: Boolean = true,
    val generalMissions: List<Pair<Mission, GeneralMissionUser>> = emptyList(),
    val weeklyMissions: List<Pair<Mission, WeeklyMissionUser>> = emptyList(),
    val error: String? = null
)

// Data class per rappresentare un elemento della legenda
data class LegendItemData(
    val title: String,
    val description: String,
    val color: Color,
    val onColor: Color,
    val icon: ImageVector
)