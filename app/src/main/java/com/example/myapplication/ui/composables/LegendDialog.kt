package com.example.myapplication.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.database.LegendItemData
import androidx.compose.runtime.derivedStateOf
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember


@Composable
fun LegendDialog(
    title: String = "Legenda",
    titleIcon: ImageVector = Icons.Default.Info,
    items: List<LegendItemData>,
    confirmButtonText: String = "Ho capito",
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()


    val isScrolledToBottom by remember {
        derivedStateOf {
            !scrollState.canScrollForward ||
                    scrollState.value >= (scrollState.maxValue * 0.95f) // 95% per una piccola tolleranza
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    titleIcon,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column {
                // Contenitore scrollabile con altezza massima
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items.forEach { item ->
                        LegendItem(
                            title = item.title,
                            description = item.description,
                            color = item.color,
                            onColor = item.onColor,
                            icon = item.icon
                        )
                    }
                }

                if (!isScrolledToBottom) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Scorri per continuare",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Scorri per continuare",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                enabled = isScrolledToBottom,
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(confirmButtonText)
            }
        }
    )
}

@Composable
private fun LegendItem(
    title: String,
    description: String,
    color: Color,
    onColor: Color,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Badge con lo stato
        Surface(
            color = color,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = onColor
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = onColor
                )
            }
        }

        // Descrizione
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
fun createRequestStatusLegendItems(): List<LegendItemData> {
    return listOf(
        LegendItemData(
            title = "Programmata",
            description = "La richiesta è stata creata e programmata per una data futura. " +
                    "È possibile modificarla, eliminarla o gestire i partecipanti.",
            color = MaterialTheme.colorScheme.primaryContainer,
            onColor = MaterialTheme.colorScheme.onPrimaryContainer,
            icon = Icons.Default.Schedule
        ),
        LegendItemData(
            title = "In preparazione",
            description = "La richiesta è programmata per domani. Non è più possibile modificarla " +
                    "o eliminarla, ma è ancora possibile gestire i partecipanti.",
            color = MaterialTheme.colorScheme.tertiaryContainer,
            onColor = MaterialTheme.colorScheme.onTertiaryContainer,
            icon = Icons.Default.Build
        ),
        LegendItemData(
            title = "In corso",
            description = "La richiesta è programmata per oggi. È possibile contrassegnarla " +
                    "come completata, ma non modificarla o eliminarla.",
            color = MaterialTheme.colorScheme.secondaryContainer,
            onColor = MaterialTheme.colorScheme.onSecondaryContainer,
            icon = Icons.Default.PlayArrow
        ),
        LegendItemData(
            title = "Scaduta",
            description = "La richiesta era programmata per una data passata ed è stata " +
                    "completata automaticamente dal sistema.",
            color = MaterialTheme.colorScheme.errorContainer,
            onColor = MaterialTheme.colorScheme.onErrorContainer,
            icon = Icons.Default.Schedule
        )
    )
}

@Composable
fun createInfoAppLegendItems(): List<LegendItemData> {
    return listOf(
        LegendItemData(
            title = "Overview",
            description = "Ecco getRescued! Un app che permette ai volontari di creare richieste " +
                    "d’aiuto, formando una squadra da una o più persone.\n",
            color = MaterialTheme.colorScheme.primaryContainer,
            onColor = MaterialTheme.colorScheme.onPrimaryContainer,
            icon = Icons.Default.AutoAwesome
        ),
        LegendItemData(
            title = "Profilo",
            description = "Da qui puoi vedere e modificare le tue informazioni personali come " +
                    "le tue abilità (Tags), cambiare titolo e visualizzare il tuo livello di " +
                    "esperienza. Puoi anche inserire una foto profilo cliccando sull'icona " +
                    "circolare con le tue iniziali (nota: la foto sarà " +
                    "visualizzabile ai creatori delle richieste) ",
            color = MaterialTheme.colorScheme.tertiaryContainer,
            onColor = MaterialTheme.colorScheme.onTertiaryContainer,
            icon = Icons.Default.Build
        ),
        LegendItemData(
            title = "Richieste",
            description = "Tutti gli utenti possono creare una richesta. Per partecipare è necessario " +
                    "avere le abilità adeguate(Tags). Ricordatevi di accettare i partecipanti se" +
                    " siete i creatori della richiesta! (maggiori info nella sezione " +
                    "'Crea richiesta')",
            color = MaterialTheme.colorScheme.secondaryContainer,
            onColor = MaterialTheme.colorScheme.onSecondaryContainer,
            icon = Icons.Default.PlayArrow
        ),
        LegendItemData(
            title = "Missioni",
            description = "GetRescued ti permette di fare volontariato divertendoti! " +
                    "Sono presenti missioni generali e settimanali per guadagnare punti esperienza " +
                    "(exp) e salire di livello. Completando missioni generali si ottengono " +
                    "exp una tantum e si ottengono titoli per personalizzare il proprio profilo, " +
                    "mentre le missioni settimanali offrono exp ogni settimana. " +
                    "Le missioni settimanali si resettano ogni lunedì alle 00:00.",
            color = MaterialTheme.colorScheme.errorContainer,
            onColor = MaterialTheme.colorScheme.onErrorContainer,
            icon = Icons.Default.Schedule
        )
    )
}