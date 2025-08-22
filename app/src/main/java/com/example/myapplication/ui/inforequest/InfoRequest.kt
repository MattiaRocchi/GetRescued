package com.example.myapplication.ui.inforequest

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.database.Request
import com.example.myapplication.ui.composables.InfoRow
import openAddressInMaps
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoRequestScreen(
    navController: NavController,
    requestId: Int,
    viewModel: InfoRequestViewModel = koinViewModel()
) {
    val uiState = viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(requestId) {
        viewModel.loadRequest(requestId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dettaglio Richiesta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.value is InfoRequestViewModel.UiState.Success) {
                val request = (uiState as InfoRequestViewModel.UiState.Success).request
                FloatingActionButton(
                    onClick = { shareRequestDetails(context, request) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Outlined.Share, contentDescription = "Condividi richiesta")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState.value) {
                InfoRequestViewModel.UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is InfoRequestViewModel.UiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is InfoRequestViewModel.UiState.Success -> {
                    RequestDetailsContent(
                        request = state.request,
                        onParticipateClick = { viewModel.participateInRequest(state.request) },
                        onOpenMapsClick = { openAddressInMaps(context, state.request.place ?: "") }
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestDetailsContent(
    request: Request,
    onParticipateClick: () -> Unit,
    onOpenMapsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header con titolo
        Text(
            text = request.title,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Informazioni principali
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(
                    label = "Difficoltà",
                    value = request.difficulty,
                    icon = null
                )

                InfoRow(
                    label = "Persone richieste",
                    value = request.peopleRequired.toString(),
                    icon = null
                )

                InfoRow(
                    label = "Soccorritori attuali",
                    value = "${request.rescuers.size}/${request.peopleRequired}",
                    icon = null
                )

                if (!request.place.isNullOrEmpty()) {
                    InfoRow(
                        label = "Luogo",
                        value = request.place,
                        icon = Icons.Outlined.LocationOn
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Descrizione
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Descrizione",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = request.description,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottoni azione
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!request.place.isNullOrEmpty()) {
                Button(
                    onClick = onOpenMapsClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = "Mappa",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apri in Maps")
                }
            }

            Button(
                onClick = onParticipateClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = request.rescuers.size < request.peopleRequired
            ) {
                Text("Partecipa alla richiesta")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Data creazione
        Text(
            text = "Creata il: ${formatDate(request.date)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun shareRequestDetails(context: android.content.Context, request: Request) {
    val shareText = """
        📋 ${request.title}
        
        📝 Descrizione: ${request.description}
        🎯 Difficoltà: ${request.difficulty}
        👥 Persone richieste: ${request.peopleRequired}
        🗺️ Luogo: ${request.place ?: "Non specificato"}
        
        Vieni a dare una mano!
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Condividi richiesta")
    context.startActivity(shareIntent)
}

private fun formatDate(timestamp: Long): String {
    return android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", timestamp).toString()
}