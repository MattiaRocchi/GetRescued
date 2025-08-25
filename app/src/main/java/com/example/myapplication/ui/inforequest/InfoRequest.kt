package com.example.myapplication.ui.inforequest

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoRequestScreen(
    navController: NavController,
    viewModel: InfoRequestViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // ascolta eventi one-shot e mostra snackbar
    LaunchedEffect(viewModel) {
        viewModel.events.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        topBar = {
            // Material3: SmallTopAppBar è stabile e compatibile
            TopAppBar(
                title = { Text("Dettaglio richiesta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)) {

            when (uiState) {
                is InfoRequestViewModel.UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is InfoRequestViewModel.UiState.NotFound -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Richiesta non trovata")
                    }
                }

                is InfoRequestViewModel.UiState.Ready -> {
                    val state = uiState as InfoRequestViewModel.UiState.Ready
                    val r = state.request
                    val creatorName: String = run {
                        val creator = state.creator
                        if (creator == null) {
                            "Sconosciuto"
                        } else {
                            // attenzione: qui assumiamo che UserWithInfo abbia una proprietà 'user' di tipo User?
                            val user = creator
                            if (user == null) "Sconosciuto" else "${user.name} ${user.surname}"
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = r.title, style = MaterialTheme.typography.titleLarge)
                                Text(text = "Creato da: $creatorName", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Difficoltà: ${r.difficulty}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Persone richieste: ${r.peopleRequired}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Partecipanti: ${r.rescuers.size}", style = MaterialTheme.typography.bodyMedium)
                                r.place?.let { Text(text = "Luogo: $it", style = MaterialTheme.typography.bodyMedium) }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Descrizione:", style = MaterialTheme.typography.titleSmall)
                                Text(r.description, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        val enabled = !state.isCreator && !state.isParticipating && !state.isFull

                        Button(
                            onClick = { viewModel.participate() },
                            enabled = enabled,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val label = when {
                                state.isCreator -> "Sei il creatore"
                                state.isParticipating -> "Stai già partecipando"
                                state.isFull -> "Posti esauriti"
                                else -> "Partecipa"
                            }
                            Text(label)
                        }
                    }
                }
            }
        }
    }
}