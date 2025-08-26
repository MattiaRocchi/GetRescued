package com.example.myapplication.ui.missions

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.myapplication.data.database.Mission
import com.example.myapplication.data.database.generalMissionUser
import com.example.myapplication.ui.composables.MissionCardGeneral

@Composable
fun MissionWeekScreen(
    navController: NavHostController,
    viewModel: MissionViewModel,
){
    val sampleMission = Mission(
        id = 1,
        name = "Accetta 3 richieste:",
        description = "Completa 3 richieste",
        exp = 150
    )

    val sampleGeneralMissionUser = generalMissionUser(
        id = 1,
        idUser = 1,
        progression = 1,
        active = true
    )

    Column {
        MissionCardGeneral(
            mission = sampleMission,
            generalMissionUser = sampleGeneralMissionUser,
            onClaimClick = { /* Azione quando si clicca Claim */ }
        )

        // Esempio con missione completata
        val completedMission = sampleMission.copy(name = "Accetta 1 richiesta con il tag Idraulico:")
        val completedMissionUser = sampleGeneralMissionUser.copy(progression = 3)

        MissionCardGeneral(
            mission = completedMission.copy(exp = 75),
            generalMissionUser = completedMissionUser,
            onClaimClick = { /* Azione quando si clicca Claim */ }
        )
    }
}