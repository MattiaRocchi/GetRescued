package com.example.myapplication.ui.addrequest

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Crea una nuova richiesta", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = title, onValueChange = viewModel::onTitleChange,
            label = { Text("Titolo") }, modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description, onValueChange = viewModel::onDescriptionChange,
            label = { Text("Descrizione") }, modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = peopleRequired.toString(),
            onValueChange = { it.toIntOrNull()?.let(viewModel::onPeopleRequiredChange) },
            label = { Text("Numero persone richieste") },
            modifier = Modifier.fillMaxWidth()
        )

        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedTextField(
                value = difficulty, onValueChange = {},
                readOnly = true, label = { Text("Difficoltà") },
                modifier = Modifier.fillMaxWidth().clickable { expanded = true }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf("Bassa", "Media", "Alta").forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = {
                        viewModel.onDifficultyChange(it); expanded = false
                    })
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = location, onValueChange = viewModel::onLocationChange,
            label = { Text("Posizione (es: Piazza Duomo, Milano)") },
            modifier = Modifier.fillMaxWidth()
        )

        if (location.isNotBlank()) {
            Button(
                onClick = { openAddressInMaps(context, location) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) { Text("Visualizza posizione in Maps") }
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { viewModel.submitRequest(onCreated) },
            modifier = Modifier.align(Alignment.End)
        ) { Text("Crea Richiesta") }
    }
}