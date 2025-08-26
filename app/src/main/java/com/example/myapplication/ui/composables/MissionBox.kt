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
import androidx.compose.material.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.database.Mission
import com.example.myapplication.data.database.generalMissionUser
import com.example.myapplication.data.database.settimanalMissionUser
import com.example.myapplication.ui.theme.UnpressableButton


@Composable
fun MissionCardWeek(
    mission: Mission,
    settimanalMissionUser: settimanalMissionUser? = null,
    onClaimClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Determina quale tipo di missione stiamo usando
    val missionUser = settimanalMissionUser
    val progression = missionUser?.progression ?: 0
    val isActive = missionUser?.active ?: true

    // Calcola se la missione è completata (assumo che 3 sia il target per le richieste)
    val isCompleted = progression >= 3 // Puoi modificare questa logica secondo le tue esigenze

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer // Colore verde acqua del mock-up
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
                            .fillMaxWidth(0.6f) // Regola la larghezza della barra
                            .height(24.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        // Progress fill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progression / 3f) // Assumo target di 3
                                .fillMaxHeight()
                                .background(
                                    if (isCompleted) MaterialTheme.colorScheme.tertiaryContainer else UnpressableButton,
                                    RoundedCornerShape(12.dp)
                                )
                        )

                        // Testo progress centrato
                        Text(
                            text = "$progression/3", // Modifica secondo la tua logica
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
                    enabled = isCompleted && isActive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) MaterialTheme.colorScheme.secondary else UnpressableButton,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Claim",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
fun MissionCardGeneral(
    mission: Mission,
    generalMissionUser: generalMissionUser? = null,
    settimanalMissionUser: settimanalMissionUser? = null,
    onClaimClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Determina quale tipo di missione stiamo usando
    val missionUser = generalMissionUser
    val progression = missionUser?.progression ?: 0
    val isActive = missionUser?.active ?: true

    // Calcola se la missione è completata (assumo che 3 sia il target per le richieste)
    val isCompleted = progression >= 3 // Puoi modificare questa logica secondo le tue esigenze

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer // Colore verde acqua del mock-up
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
                            .fillMaxWidth(0.6f) // Regola la larghezza della barra
                            .height(24.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        // Progress fill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progression / 3f) // Assumo target di 3
                                .fillMaxHeight()
                                .background(
                                    if (isCompleted) MaterialTheme.colorScheme.tertiaryContainer else UnpressableButton,
                                    RoundedCornerShape(12.dp)
                                )
                        )

                        // Testo progress centrato
                        Text(
                            text = "$progression/3", // Modifica secondo la tua logica
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
                    enabled = isCompleted && isActive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) MaterialTheme.colorScheme.secondary else UnpressableButton,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Claim",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
