package com.example.myapplication.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.database.Request
import com.example.myapplication.ui.theme.DifficulTask
import com.example.myapplication.ui.theme.EasyTask
import com.example.myapplication.ui.theme.MediumTask
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card condivisa per visualizzare una richiesta con colori dinamici
 */
@Composable
fun DynamicRequestCard(
    request: Request,
    onClick: () -> Unit,
    currentUserId: Int = -1 // Aggiungiamo il parametro per l'ID utente corrente
) {
    val backgroundColor = when (request.difficulty) {
        "Bassa" -> EasyTask
        "Media" -> MediumTask
        "Alta" -> DifficulTask
        else -> EasyTask
    }

    val isCompleted = request.rescuers.size >= request.peopleRequired
    val isCreatedByCurrentUser = currentUserId != -1 && currentUserId == request.sender

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column {
            // Barra colorata in base alla difficoltà
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(backgroundColor)
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = request.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        color = backgroundColor.copy(
                            red = (backgroundColor.red * 1.1f).coerceAtMost(1f),
                            green = (backgroundColor.green * 1.6f).coerceAtMost(1f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .border(
                                width = 2.dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Text(
                            text = request.difficulty,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = request.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )

                request.place?.let { place ->
                    Text(
                        text = "📍 $place",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👥 ${request.rescuers.size}/${request.peopleRequired}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )

                        if (isCompleted) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "✓ Completo",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Data di creazione
                    Text(
                        text = remember(request.date) {
                            java.text.SimpleDateFormat("dd/MM/yy", java.util.Locale.getDefault())
                                .format(java.util.Date(request.date))
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Etichetta creatore - MOSTRA SOLO SE È CREATA DALL'UTENTE CORRENTE
                if (isCreatedByCurrentUser) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "👤 Creata da te",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Chip per mostrare la difficoltà con colori dinamici
 */
@Composable
fun DifficultyChip(
    difficulty: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = backgroundColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            text = difficulty,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Chip per mostrare che la richiesta è completa
 */
@Composable
fun CompletedChip() {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "✓ Completo",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Testo formattato per la data
 */
@Composable
fun DateText(date: Long) {
    Text(
        text = remember(date) {
            SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                .format(Date(date))
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * Filtro per le richieste
 */
@Composable
fun RequestFilter(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedDifficulty: String?,
    onDifficultyChange: (String?) -> Unit,
    sortByDate: Boolean,
    onSortByDateChange: (Boolean) -> Unit,
    showFilters: Boolean,
    onToggleFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Cerca richieste...") },
                leadingIcon = {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Search,
                        contentDescription = "Cerca"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onToggleFilters) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.FilterList,
                            contentDescription = "Filtri"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (showFilters) {
                Spacer(Modifier.height(12.dp))

                // Filtro difficoltà
                Text("Difficoltà:", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    FilterChip(
                        onClick = { onDifficultyChange(if (selectedDifficulty == "Bassa") null else "Bassa") },
                        label = { Text("Bassa") },
                        selected = selectedDifficulty == "Bassa",
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EasyTask
                        )
                    )
                    FilterChip(
                        onClick = { onDifficultyChange(if (selectedDifficulty == "Media") null else "Media") },
                        label = { Text("Media") },
                        selected = selectedDifficulty == "Media",
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MediumTask
                        )
                    )
                    FilterChip(
                        onClick = { onDifficultyChange(if (selectedDifficulty == "Alta") null else "Alta") },
                        label = { Text("Alta") },
                        selected = selectedDifficulty == "Alta",
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DifficulTask
                        )
                    )
                }

                // Ordinamento per data
                Text("Ordinamento:", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    FilterChip(
                        onClick = { onSortByDateChange(true) },
                        label = { Text("Più recenti") },
                        selected = sortByDate
                    )
                    FilterChip(
                        onClick = { onSortByDateChange(false) },
                        label = { Text("Più vecchi") },
                        selected = !sortByDate
                    )
                }
            }
        }
    }
}