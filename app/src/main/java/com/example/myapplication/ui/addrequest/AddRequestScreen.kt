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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.PermissionChecker
import coil.compose.AsyncImage
import com.example.myapplication.ui.composables.CameraCapture
import com.example.myapplication.ui.composables.ImagePickerDialog
import openAddressInMaps

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
    val requiredBadges by viewModel.requiredBadges.collectAsState()
    val availableBadges by viewModel.availableBadges.collectAsState()
    val isFormValid by viewModel.isFormValid.collectAsState()

    val context = LocalContext.current

    var showImagePicker by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    var showBadgeDialog by remember { mutableStateOf(false) }

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
                    isError = title.isBlank()
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Descrizione *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    isError = description.isBlank()
                )
            }

            item {
                OutlinedTextField(
                    value = peopleRequired.toString(),
                    onValueChange = { it.toIntOrNull()?.let(viewModel::onPeopleRequiredChange) },
                    label = { Text("Numero persone richieste *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = peopleRequired <= 0
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
                        modifier = Modifier.fillMaxWidth().clickable { expanded = true }
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

            item {
                OutlinedTextField(
                    value = location,
                    onValueChange = viewModel::onLocationChange,
                    label = { Text("Posizione (es: Piazza Duomo, Milano) *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = location.isBlank()
                )

                if (location.isNotBlank()) {
                    Button(
                        onClick = { openAddressInMaps(context, location) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
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

            // Sezione badge richiesti
            item {
                Text("Badge richiesti (opzionale)", style = MaterialTheme.typography.titleMedium)

                if (requiredBadges.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        items(requiredBadges) { badge ->
                            FilterChip(
                                onClick = { viewModel.removeRequiredBadge(badge) },
                                label = { Text(badge.name) },
                                selected = true,
                                trailingIcon = { Icon(Icons.Default.Close, "Rimuovi") }
                            )
                        }
                    }
                }

                Button(
                    onClick = { showBadgeDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aggiungi badge richiesto")
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.submitRequest(onCreated) },
                    enabled = isFormValid,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crea Richiesta")
                }

                if (!isFormValid) {
                    Text(
                        "Compila tutti i campi obbligatori (*)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
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
                galleryLauncher.launch("image/*")
            }
        )
    }

    // Dialog per selezione badge
    if (showBadgeDialog) {
        AlertDialog(
            onDismissRequest = { showBadgeDialog = false },
            title = { Text("Seleziona badge richiesti") },
            text = {
                LazyColumn {
                    items(availableBadges) { badge ->
                        FilterChip(
                            onClick = {
                                viewModel.addRequiredBadge(badge)
                                showBadgeDialog = false
                            },
                            label = { Text(badge.name) },
                            selected = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBadgeDialog = false }) {
                    Text("Chiudi")
                }
            }
        )
    }
}