package com.example.myapplication.ui.composables


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LegendDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Stati della richiesta",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Programmata
                LegendItem(
                    title = "Programmata",
                    description = "La richiesta è stata creata e programmata per una data futura. È possibile modificarla, eliminarla o gestire i partecipanti.",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    onColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    icon = Icons.Default.Schedule
                )

                // In preparazione
                LegendItem(
                    title = "In preparazione",
                    description = "La richiesta è programmata per domani. Non è più possibile modificarla o eliminarla, ma è ancora possibile gestire i partecipanti.",
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    onColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    icon = Icons.Default.Build
                )

                // In corso
                LegendItem(
                    title = "In corso",
                    description = "La richiesta è programmata per oggi. È possibile contrassegnarla come completata, ma non modificarla o eliminarla.",
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    onColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    icon = Icons.Default.PlayArrow
                )

                // Scaduta
                LegendItem(
                    title = "Scaduta",
                    description = "La richiesta era programmata per una data passata ed è stata completata automaticamente dal sistema.",
                    color = MaterialTheme.colorScheme.errorContainer,
                    onColor = MaterialTheme.colorScheme.onErrorContainer,
                    icon = Icons.Default.Schedule
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Ho capito")
            }
        }
    )
}

@Composable
private fun LegendItem(
    title: String,
    description: String,
    color: androidx.compose.ui.graphics.Color,
    onColor: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
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