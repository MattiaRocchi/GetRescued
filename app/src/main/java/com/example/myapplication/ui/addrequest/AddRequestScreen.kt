package com.example.myapplication.ui.addrequest

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.PermissionChecker
import coil.compose.AsyncImage
import com.example.myapplication.ui.composables.CameraCapture
import com.example.myapplication.ui.composables.ImagePickerDialog
import com.example.myapplication.ui.theme.UnpressableButtonDark
import com.google.android.gms.common.SignInButton
import openAddressInMaps
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRequestScreen(
    viewModel: AddRequestViewModel,
    onCreated: () -> Unit
) {
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val peopleRequired by viewModel.peopleRequired.collectAsState()
    val difficulty by viewModel.difficulty.collectAsState()
    val location by viewModel.location.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val scheduledDate by viewModel.scheduledDate.collectAsState()
    val requiredTags by viewModel.requiredTags.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()
    val isFormValid by viewModel.isFormValid.collectAsState()

    val context = LocalContext.current

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

    // Launcher per la galleria
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addPhoto(it) }
    }

    // Launcher per i permessi camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showCamera = true
        }
    }

    if (showCamera) {
        CameraCapture(
            onImageFile = { uri ->
                viewModel.addPhoto(uri)
                showCamera = false
            },
            onBack = { showCamera = false }
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Crea una nuova richiesta", style = MaterialTheme.typography.titleLarge)
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = viewModel::onTitleChange,
                    label = { Text("Titolo *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = title.isBlank(),
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) }
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Descrizione *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    isError = description.isBlank(),
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) }
                )
            }

            item {
                OutlinedTextField(
                    value = peopleRequired.toString(),
                    onValueChange = { it.toIntOrNull()?.let(viewModel::onPeopleRequiredChange) },
                    label = { Text("Numero persone richieste *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = peopleRequired <= 0,
                    leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) }
                )
            }

            item {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = difficulty,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Difficoltà *") },
                        modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                        leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
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

            // Campo data di svolgimento
            item {
                OutlinedTextField(
                    value = scheduledDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Data di svolgimento *") },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
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

            item {
                OutlinedTextField(
                    value = location,
                    onValueChange = viewModel::onLocationChange,
                    label = { Text("Posizione (es: Piazza Duomo, Milano) *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = location.isBlank(),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                )

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

            // Sezione foto
            item {
                Text("Foto (opzionale)", style = MaterialTheme.typography.titleMedium)

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(photos) { photoUri ->
                        Box(modifier = Modifier.size(100.dp)) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Foto richiesta",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = {
                                    val index = photos.indexOf(photoUri)
                                    viewModel.removePhoto(index)
                                },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, "Rimuovi foto")
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { showImagePicker = true },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, "Aggiungi foto")
                            }
                        }
                    }
                }
            }

            // Sezione tag richiesti
            item {
                Text("Tag richiesti (opzionale)", style = MaterialTheme.typography.titleMedium)

                if (requiredTags.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
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
                }

                Button(
                    onClick = { showTagDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Aggiungi tag richiesto")
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.submitRequest(onCreated) },
                    enabled = isFormValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                        disabledContainerColor = UnpressableButtonDark,
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Crea Richiesta")
                }

                if (!isFormValid) {
                    Text(
                        "Compila tutti i campi obbligatori (*) e assicurati che la data sia valida",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
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
                galleryLauncher.launch("image/*")
            }
        )
    }

    // Dialog per selezione tag
    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Seleziona tag richiesti") },
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