package com.example.myapplication.ui.managerequest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import openAddressInMaps
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRequest(
    navController: NavController,
    viewModel: ManageRequestViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Stati per i dialog di conferma
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }

    // Ascolta eventi one-shot e mostra snackbar
    LaunchedEffect(viewModel) {
        viewModel.events.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestione richiesta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    // Bottone per modificare la richiesta (solo se può essere modificata)
                    if (uiState is ManageRequestViewModel.UiState.Ready) {
                        val state = uiState as ManageRequestViewModel.UiState.Ready
                        if (state.canEdit) {
                            IconButton(
                                onClick = {
                                    navController.navigate(
                                        com.example.myapplication.ui.GetRescuedRoute.EditRequest(state.request.id)
                                    )
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Modifica")
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            when (uiState) {
                is ManageRequestViewModel.UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ManageRequestViewModel.UiState.NotFound -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Richiesta non trovata")
                    }
                }

                is ManageRequestViewModel.UiState.NotAuthorized -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Non sei autorizzato a gestire questa richiesta",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                is ManageRequestViewModel.UiState.Ready -> {
                    val state = uiState as ManageRequestViewModel.UiState.Ready
                    val r = state.request

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Card principale con dettagli della richiesta
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(
                                Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = r.title,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "La tua richiesta",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Badge stato
                                        Surface(
                                            color = when (state.requestState) {
                                                "Scaduta" -> MaterialTheme.colorScheme.errorContainer
                                                "In corso" -> MaterialTheme.colorScheme.secondaryContainer
                                                "In preparazione" -> MaterialTheme.colorScheme.tertiaryContainer
                                                "Programmata" -> MaterialTheme.colorScheme.primaryContainer
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = state.requestState,
                                                modifier = Modifier.padding(
                                                    horizontal = 8.dp,
                                                    vertical = 4.dp
                                                ),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = when (state.requestState) {
                                                    "Scaduta" -> MaterialTheme.colorScheme.onErrorContainer
                                                    "In corso" -> MaterialTheme.colorScheme.onSecondaryContainer
                                                    "In preparazione" -> MaterialTheme.colorScheme.onTertiaryContainer
                                                    "Programmata" -> MaterialTheme.colorScheme.onPrimaryContainer
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }

                                        // Badge difficoltà
                                        Surface(
                                            color = when (r.difficulty) {
                                                "Bassa" -> MaterialTheme.colorScheme.primaryContainer
                                                "Media" -> MaterialTheme.colorScheme.secondaryContainer
                                                "Alta" -> MaterialTheme.colorScheme.errorContainer
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = r.difficulty,
                                                modifier = Modifier.padding(
                                                    horizontal = 8.dp,
                                                    vertical = 4.dp
                                                ),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider()

                                // Informazioni richiesta
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "👥 Persone richieste: ${r.peopleRequired}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "🤝 Richieste ricevute: ${state.pendingParticipants.size}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "✅ Approvati: ${r.rescuers.size}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                // Data di svolgimento
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val scheduledDate =
                                        java.time.Instant.ofEpochMilli(r.scheduledDate)
                                            .atZone(java.time.ZoneId.systemDefault())
                                            .toLocalDate()
                                    Text(
                                        "📅 Data: ${
                                            scheduledDate.format(
                                                DateTimeFormatter.ofLocalizedDate(
                                                    FormatStyle.MEDIUM
                                                )
                                            )
                                        }",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                // Posizione con bottone Maps
                                r.place?.let { place ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📍 $place",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedButton(
                                            onClick = { openAddressInMaps(context, place) },
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = "Apri in Maps",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text("Maps")
                                        }
                                    }
                                }

                                HorizontalDivider()

                                Text("📝 Descrizione:", style = MaterialTheme.typography.titleSmall)
                                Text(r.description, style = MaterialTheme.typography.bodyMedium)

                                if (state.requestTags.isNotEmpty()) {
                                    HorizontalDivider()

                                    Text(
                                        "🏷️ Tag richiesti:",
                                        style = MaterialTheme.typography.titleSmall
                                    )

                                    @OptIn(ExperimentalLayoutApi::class)
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        state.requestTags.forEach { tag ->
                                            Surface(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Tag,
                                                        contentDescription = "Tag",
                                                        modifier = Modifier.size(14.dp),
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                    Spacer(Modifier.width(4.dp))
                                                    Text(
                                                        text = tag.name,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Pulsanti di azione
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "Azioni disponibili:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Pulsante elimina (solo se può essere eliminata)
                                        if (state.canDelete) {
                                            OutlinedButton(
                                                onClick = { showDeleteDialog = true },
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Elimina",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text("Elimina")
                                            }
                                        }

                                        // Pulsante completa (solo se può essere completata)
                                        if (state.canMarkCompleted) {
                                            Button(
                                                onClick = { showCompleteDialog = true },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.secondary
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = "Completa",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text("Completa")
                                            }
                                        }
                                    }
                                }

                                //Messaggi informativi:
                                Spacer(Modifier.height(8.dp))
                                when (state.requestState) {
                                    "Scaduta" -> {
                                        Text(
                                            "⚠️ La richiesta è scaduta. Verrà completata automaticamente.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                    "In corso" -> {
                                        Text(
                                            "▶️ La richiesta è in corso oggi. Puoi contrassegnarla come completata.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    "In preparazione" -> {
                                        Text(
                                            "⏳ La richiesta è in preparazione per domani. Nessuna azione disponibile.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    "Programmata" -> {
                                        Text(
                                            "📅 Puoi modificare, eliminare la richiesta o gestire i partecipanti.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}