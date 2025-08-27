package com.example.myapplication.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.missions.MissionWithProgress
import com.example.myapplication.ui.theme.UnpressableButton

@Composable
fun MissionCard(
    missionWithProgress: MissionWithProgress,
    onClaimClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val mission = missionWithProgress.mission
    val progression = missionWithProgress.progression
    val requirement = mission.requirement
    val isCompleted = missionWithProgress.isCompleted
    val canClaim = missionWithProgress.canClaim
    val isActive = missionWithProgress.isActive

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colonna sinistra con testo e progress
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Titolo della missione
                Text(
                    text = mission.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Progress indicator
                Column {
                    // Barra di progresso personalizzata
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(24.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        // Progress fill - calcolo corretto della percentuale
                        val progressPercentage = if (requirement > 0) {
                            (progression.toFloat() / requirement.toFloat()).coerceIn(0f, 1f)
                        } else {
                            0f
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressPercentage)
                                .fillMaxHeight()
                                .background(
                                    if (isCompleted) MaterialTheme.colorScheme.tertiaryContainer else UnpressableButton,
                                    RoundedCornerShape(12.dp)
                                )
                        )

                        // Testo progress centrato
                        Text(
                            text = "$progression/$requirement",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            // Colonna destra con EXP e bottone
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Badge EXP
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.tertiary
                ) {
                    Text(
                        text = "${mission.exp ?: 0} exp",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                // Bottone Claim
                Button(
                    onClick = onClaimClick,
                    enabled = canClaim,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canClaim) MaterialTheme.colorScheme.secondary else UnpressableButton,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = when {
                            !isActive -> "Completata"
                            canClaim -> "Claim"
                            isCompleted -> "Claimable"
                            else -> "In Corso"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Mantieni le vecchie versioni per backward compatibility se necessario
@Composable
fun MissionCardWeek(
    mission: com.example.myapplication.data.database.Mission,
    weeklyMissionUser: com.example.myapplication.data.database.WeeklyMissionUser? = null,
    onClaimClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val missionWithProgress = MissionWithProgress(
        mission = mission,
        weeklyMissionUser = weeklyMissionUser,
        isCompleted = (weeklyMissionUser?.progression ?: 0) >= mission.requirement,
        canClaim = weeklyMissionUser?.claimable == true && weeklyMissionUser.active
    )

    MissionCard(
        missionWithProgress = missionWithProgress,
        onClaimClick = onClaimClick,
        modifier = modifier
    )
}

@Composable
fun MissionCardGeneral(
    mission: com.example.myapplication.data.database.Mission,
    generalMissionUser: com.example.myapplication.data.database.GeneralMissionUser? = null,
    weeklyMissionUser: com.example.myapplication.data.database.WeeklyMissionUser? = null,
    onClaimClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val missionWithProgress = MissionWithProgress(
        mission = mission,
        generalMissionUser = generalMissionUser,
        weeklyMissionUser = weeklyMissionUser,
        isCompleted = (generalMissionUser?.progression ?: 0) >= mission.requirement,
        canClaim = generalMissionUser?.claimable == true && generalMissionUser.active
    )

    MissionCard(
        missionWithProgress = missionWithProgress,
        onClaimClick = onClaimClick,
        modifier = modifier
    )
}