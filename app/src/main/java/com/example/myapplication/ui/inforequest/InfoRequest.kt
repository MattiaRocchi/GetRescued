package com.example.myapplication.ui.inforequest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
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
import openAddressInMaps
import java.text.SimpleDateFormat
import java.util.*



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoRequestScreen(
    navController: NavController,
    viewModel: InfoRequestViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val requestTags by viewModel.requestTags.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Ascolta eventi one-shot e mostra snackbar
    LaunchedEffect(viewModel) {
        viewModel.events.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        topBar = {
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
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)) {

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
                            "${creator.name} ${creator.surname}"
                        }
                    }
                    val creatorEmail: String = run {
                        val creator = state.creator
                        creator?.email ?: "Sconosciuto"
                    }
                    val creatorPhone: String = run {
                        val creator = state.creator
                        if (creator == null) {
                            "Sconosciuto"
                        } else {
                            "${creator.phoneNumber}"
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Card principale con dettagli
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = r.title,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ){
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
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }


                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Creato da: $creatorName",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Email,
                                            contentDescription = "Email",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Email: $creatorEmail",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Phone,
                                            contentDescription = "Phone",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Phone: $creatorPhone",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                HorizontalDivider()


                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.People,
                                            contentDescription = "Persone richieste",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("Persone richieste: ${r.peopleRequired}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Handshake,
                                            contentDescription = "Partecipanti",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("Partecipanti: ${r.rescuers.size}", style = MaterialTheme.typography.bodyMedium)
                                    }


                                // Data di svolgimento prevista
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = "Data prevista",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Data prevista: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(r.scheduledDate))}",
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = "Posizione",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = place,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
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

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = "Descrizione",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Descrizione:", style = MaterialTheme.typography.titleSmall)
                                }
                                Text(r.description, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        // Sezione tag richiesti
                        if (requestTags.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Label,
                                            contentDescription = "Tag richiesti",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "Tag richiesti",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(requestTags) { tag ->
                                            AssistChip(
                                                onClick = { },
                                                label = { Text(tag.name) },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Tag,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        // Sezione foto (se presenti)
                        if (r.fotos.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.PhotoCamera,
                                            contentDescription = "Foto allegate",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            "Foto allegate",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.height(120.dp)
                                    ) {
                                        items(r.fotos) { photoUri ->
                                            AsyncImage(
                                                model = photoUri,
                                                contentDescription = "Foto della richiesta",
                                                modifier = Modifier
                                                    .size(120.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Spazio prima dei pulsanti
                        Spacer(Modifier.height(24.dp))

                        // Pulsanti di azione
                        when {
                            state.isCreator -> {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = "Creatore",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            "Sei il creatore di questa richiesta",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            state.isParticipating -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = "Approvato",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                "Sei stato approvato per questa richiesta!",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.leaveRequest() },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.ExitToApp,
                                            contentDescription = "Mi tiro indietro",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("Mi tiro indietro")
                                    }
                                }
                            }

                            state.isPending -> {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Schedule,
                                            contentDescription = "In attesa",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            "Richiesta di partecipazione inviata, attendi l'approvazione del creatore",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            state.isFull -> {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Block,
                                            contentDescription = "Posti esauriti",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            "Posti esauriti",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            else -> {
                                val canUserParticipate by viewModel.canParticipate.collectAsStateWithLifecycle()
                                if (canUserParticipate || requestTags.isEmpty()) {
                                    Button(
                                        onClick = { viewModel.participate() },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Handshake,
                                            contentDescription = "Richiedi di partecipare",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("Richiedi di partecipare")
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Warning,
                                                        contentDescription = "Avvertimento",
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                    Text(
                                                        "Non puoi partecipare a questa richiesta",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                }
                                                Text(
                                                    "Ti mancano alcuni tag richiesti. Aggiorna il tuo profilo per partecipare.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
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
    }
}