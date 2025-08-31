package com.example.myapplication.ui.managerequest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.myapplication.ui.GetRescuedRoute
import com.example.myapplication.ui.composables.LegendDialog
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
    var showExpiredDialog by remember { mutableStateOf(false) }
    var showStatusLegend by remember { mutableStateOf(false) }

    // Ascolta eventi one-shot e mostra snackbar
    LaunchedEffect(viewModel) {
        viewModel.events.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is ManageRequestViewModel.UiState.Ready && state.isExpired) {
            showExpiredDialog = true
        }
        if(state is ManageRequestViewModel.UiState.Ready && state.isComplete){
            navController.navigate(GetRescuedRoute.ManageRequests)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestione richiesta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
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
                                        GetRescuedRoute.EditRequest(state.request.id)
                                    )
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Modifica")
                            }
                        }
                        IconButton(onClick = { showStatusLegend = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Info stati")
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
                    navController.navigate(GetRescuedRoute.ManageRequests)
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
                                            Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            ) {
                                                Icon(
                                                    modifier = Modifier.size(14.dp),
                                                    imageVector = when (state.requestState) {
                                                        "Scaduta" -> Icons.Default.Schedule
                                                        "In corso" -> Icons.Default.PlayArrow
                                                        "In preparazione" -> Icons.Default.Build
                                                        "Programmata" -> Icons.Default.Schedule
                                                        else -> Icons.Default.Schedule
                                                    },
                                                    contentDescription = null,
                                                )
                                                Text(
                                                    text = state.requestState,
                                                    modifier = Modifier.padding(
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
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Group,
                                            contentDescription = "Persone richieste",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            "Persone richieste: ${r.peopleRequired}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Handshake,
                                            contentDescription = "Richieste ricevute",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            "Richieste ricevute: ${state.pendingParticipants.size}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Approvati",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "Approvati: ${r.rescuers.size}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                // Data di svolgimento
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = "Data",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        val scheduledDate =
                                            java.time.Instant.ofEpochMilli(r.scheduledDate)
                                                .atZone(java.time.ZoneId.systemDefault())
                                                .toLocalDate()
                                        Text(
                                            "Data: ${
                                                scheduledDate.format(
                                                    DateTimeFormatter.ofLocalizedDate(
                                                        FormatStyle.MEDIUM
                                                    )
                                                )
                                            }",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
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

                                if (state.requestTags.isNotEmpty()) {
                                    HorizontalDivider()

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Label,
                                            contentDescription = "Tag richiesti",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            "Tag richiesti:",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }

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

                                HorizontalDivider()
                                // Sezione foto (se presenti)
                                if (r.fotos.isNotEmpty()) {
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
                        }

                        // Sezione Richieste di Partecipazione Pending (solo se può gestire partecipanti)
                        if (state.pendingParticipants.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = "Richieste pending",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Richieste di Partecipazione (${state.pendingParticipants.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    state.pendingParticipants.forEach { user ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        "${user.name} ${user.surname}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        user.email,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    IconButton(
                                                        onClick = { viewModel.approveParticipant(user.id) },
                                                        colors = IconButtonDefaults.iconButtonColors(
                                                            containerColor = MaterialTheme.colorScheme.primary,
                                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                                        )
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Check,
                                                            contentDescription = "Approva",
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                    if (state.canManageParticipants){
                                                        IconButton(
                                                            onClick = { viewModel.rejectParticipant(user.id) },
                                                            colors = IconButtonDefaults.iconButtonColors(
                                                                containerColor = MaterialTheme.colorScheme.error,
                                                                contentColor = MaterialTheme.colorScheme.onError
                                                            )
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Close,
                                                                contentDescription = "Rifiuta",
                                                                modifier = Modifier.size(18.dp)
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

                        // Sezione Partecipanti Approvati (solo se può gestire partecipanti e ci sono partecipanti)
                        if (state.canManageParticipants && state.approvedParticipants.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Group,
                                            contentDescription = "Partecipanti approvati",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Partecipanti Approvati (${state.approvedParticipants.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    state.approvedParticipants.forEach { user ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        "${user.name} ${user.surname}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        user.email,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { viewModel.removeParticipant(user.id) },
                                                    colors = IconButtonDefaults.iconButtonColors(
                                                        containerColor = MaterialTheme.colorScheme.error,
                                                        contentColor = MaterialTheme.colorScheme.onError
                                                    )
                                                ) {
                                                    Icon(
                                                        Icons.Default.PersonRemove,
                                                        contentDescription = "Rimuovi",
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Sezione informativa se non può gestire partecipanti
                        if (!state.canManageParticipants && (state.pendingParticipants.isNotEmpty() || state.approvedParticipants.isNotEmpty())) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Group,
                                            contentDescription = "Partecipanti",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            "Partecipanti",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (state.approvedParticipants.isNotEmpty()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = "Approvati",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                "Approvati: ${state.approvedParticipants.size}/${r.peopleRequired}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        state.approvedParticipants.forEach { user ->
                                            Text(
                                                "• ${user.name} ${user.surname}",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                            )
                                        }
                                    }

                                    if (state.pendingParticipants.isNotEmpty()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(top = 8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Schedule,
                                                contentDescription = "In attesa",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                "In attesa: ${state.pendingParticipants.size}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                        Text(
                                            "Non puoi più gestire le richieste di partecipazione.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                        )
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = "Avviso",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                            Text(
                                                "La richiesta è scaduta. Verrà completata automaticamente.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                    "In corso" -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.PlayArrow,
                                                contentDescription = "In corso",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                "La richiesta è in corso oggi. Puoi contrassegnarla come completata.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                    "In preparazione" -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Schedule,
                                                contentDescription = "In preparazione",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                "La richiesta è in preparazione per domani. Nessuna azione disponibile.",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                    "Programmata" -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CalendarToday,
                                                contentDescription = "Programmata",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                "Puoi modificare, eliminare la richiesta o gestire i partecipanti.",
                                                style = MaterialTheme.typography.bodySmall
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
    // Dialog di conferma eliminazione
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Conferma eliminazione") },
            text = {
                Text("Sei sicuro di voler eliminare questa richiesta? L'azione non può essere annullata.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteRequest {
                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("Elimina", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    // Dialog di conferma completamento
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Completa richiesta") },
            text = {
                Text("Confermi che la richiesta è stata portata a termine? Verranno assegnati XP e aggiornate le missioni di tutti i partecipanti.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCompleteDialog = false
                        viewModel.markAsCompleted {
                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("Completa", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }
    // Dialog per richiesta scaduta
    if (showExpiredDialog) {
        AlertDialog(
            onDismissRequest = { }, // Non permettere di chiudere senza confermare
            title = { Text("Richiesta Scaduta") },
            text = {
                Text("La richiesta è stata completata in automatico perchè scaduta.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExpiredDialog = false
                        navController.navigate(GetRescuedRoute.ManageRequests)
                    }
                ) {
                    Text("Conferma", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
    // Dialog per La legenda
    if (showStatusLegend) {
        LegendDialog(
            onDismiss = { showStatusLegend = false }
        )
    }
}