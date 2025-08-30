package com.example.myapplication.ui.editrequest

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.PermissionChecker
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.ui.composables.CameraCapture
import com.example.myapplication.ui.composables.ImagePickerDialog
import openAddressInMaps
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRequestScreen(
    navController: NavController,
    viewModel: EditRequestViewModel
) {
    val request = viewModel.requestFlow.collectAsStateWithLifecycle().value
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val difficulty by viewModel.difficulty.collectAsState()
    val peopleRequired by viewModel.peopleRequired.collectAsState()
    val location by viewModel.location.collectAsState()
    val images by viewModel.images.collectAsState()
    val scheduledDate by viewModel.scheduledDate.collectAsState()
    val requiredTags by viewModel.requiredTags.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Stati per gestire i dialog e la camera
    var showImagePicker by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // State per il DatePicker
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = java.time.ZoneId.systemDefault().let { zoneId ->
            scheduledDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        }
    )

    // Launcher per selezione immagini dalla galleria
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addImage(it.toString())
        }
    }

    // Launcher per i permessi camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showCamera = true
        }else{
            Toast.makeText(
                context,
                "Permesso fotocamera permanentemente negato. Abilitalo dalle impostazioni.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Ascolta eventi
    LaunchedEffect(viewModel) {
        viewModel.events.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (showCamera) {
        CameraCapture(
            onImageFile = { uri ->
                viewModel.addImage(uri.toString())
                showCamera = false
            },
            onBack = { showCamera = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Modifica richiesta") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->

            if (request == null) {
                Box(
                    Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Titolo
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = viewModel::onTitleChange,
                        label = { Text("Titolo") },
                        leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Descrizione
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = { Text("Descrizione") },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }

                // Numero persone richieste
                item {
                    var peopleText by remember { mutableStateOf(peopleRequired.toString()) }
                    var hasUserStartedTyping by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = peopleText,
                        onValueChange = { newText ->
                            // Se è la prima volta che l'utente digita, cancella il contenuto esistente
                            if (!hasUserStartedTyping && newText.length == peopleText.length + 1) {
                                peopleText =
                                    newText.takeLast(1) // Prendi solo l'ultimo carattere digitato
                                hasUserStartedTyping = true
                            } else {
                                peopleText = newText
                            }

                            // Se l'utente inserisce un numero valido, aggiorna il viewModel
                            peopleText.toIntOrNull()?.let { number ->
                                if (number > 0) {
                                    viewModel.onPeopleRequiredChange(number)
                                }
                            }
                        },
                        label = { Text("Numero persone richieste *") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = peopleRequired <= 0,
                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                        singleLine = true
                    )

                    // Aggiorna il testo quando cambia peopleRequired dal viewModel
                    LaunchedEffect(peopleRequired) {
                        if (!hasUserStartedTyping) {
                            peopleText = peopleRequired.toString()
                        }
                    }
                }

                // Difficoltà
                item {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedTextField(
                            value = difficulty,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Difficoltà *") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            listOf("Bassa", "Media", "Alta").forEach { diff ->
                                DropdownMenuItem(
                                    text = { Text(diff) },
                                    onClick = {
                                        viewModel.onDifficultyChange(diff)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Data di svolgimento
                item {
                    OutlinedTextField(
                        value = scheduledDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Data di svolgimento *") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null)
                            }
                        },
                        isError = scheduledDate.isBefore(LocalDate.now())
                    )

                    if (scheduledDate.isBefore(LocalDate.now())) {
                        Text(
                            "La data deve essere odierna o futura",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }

                // Posizione
                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = viewModel::onLocationChange,
                        label = { Text("Posizione") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Inserisci un indirizzo o descrizione del luogo") }
                    )

                    // Bottone per aprire Maps
                    if (location.isNotBlank()) {
                        Button(
                            onClick = { openAddressInMaps(context, location) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Visualizza posizione in Maps")
                        }
                    }
                }

                // Sezione Tag richiesti
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Tag richiesti:",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Mostra i tag selezionati
                            if (requiredTags.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    items(requiredTags) { tag ->
                                        FilterChip(
                                            onClick = { viewModel.removeRequiredTag(tag) },
                                            label = { Text(tag.name) },
                                            selected = true,
                                            trailingIcon = { Icon(Icons.Default.Close, "Rimuovi") }
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    "Nessun tag selezionato",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            // Bottone per aggiungere tag
                            OutlinedButton(
                                onClick = { showTagDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Aggiungi tag")
                            }
                        }
                    }
                }

                // Sezione immagini (resto del codice rimane uguale)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Immagini:",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showImagePicker = true }
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Aggiungi")
                                    }
                                }
                            }

                            if (images.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(images) { imageUrl ->
                                        Box {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(imageUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Immagine della richiesta",
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(
                                                        1.dp,
                                                        MaterialTheme.colorScheme.outline,
                                                        RoundedCornerShape(8.dp)
                                                    ),
                                                contentScale = ContentScale.Crop
                                            )

                                            IconButton(
                                                onClick = { viewModel.removeImage(imageUrl) },
                                                modifier = Modifier.align(Alignment.TopEnd)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = Color.Black.copy(alpha = 0.7f)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Rimuovi",
                                                        tint = Color.White,
                                                        modifier = Modifier.padding(4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Nessuna immagine aggiunta",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Bottone salva
                item {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.save { navController.popBackStack() } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Salva modifiche")
                    }
                }
            }
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()

                            if (selectedDate.isBefore(LocalDate.now())) {
                                // Non fare nulla se la data è passata
                                return@TextButton
                            }

                            viewModel.onScheduledDateChange(selectedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annulla")
                }
            }
        ) {
            DatePicker(state = datePickerState)

            // Mostra avviso se la data selezionata è nel passato
            datePickerState.selectedDateMillis?.let { millis ->
                val selectedDate = java.time.Instant.ofEpochMilli(millis)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()

                if (selectedDate.isBefore(LocalDate.now())) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            "⚠️ Seleziona una data odierna o futura",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    // Dialog per selezione immagine
    if (showImagePicker) {
        ImagePickerDialog(
            onDismiss = { showImagePicker = false },
            onTakePhoto = {
                showImagePicker = false
                val permission = Manifest.permission.CAMERA
                when (PermissionChecker.checkSelfPermission(context, permission)) {
                    PermissionChecker.PERMISSION_GRANTED -> showCamera = true
                    else -> cameraPermissionLauncher.launch(permission)
                }
            },
            onPickFromGallery = {
                showImagePicker = false
                imagePickerLauncher.launch("image/*")
            }
        )
    }

    // Dialog per selezione tag
    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Seleziona tag") },
            text = {
                LazyColumn {
                    items(availableTags.filter { it !in requiredTags }) { tag ->
                        FilterChip(
                            onClick = {
                                viewModel.addRequiredTag(tag)
                                showTagDialog = false
                            },
                            label = { Text(tag.name) },
                            selected = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text("Chiudi")
                }
            }
        )
    }
}