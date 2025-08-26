package com.example.myapplication.ui.editrequest

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@Composable
fun EditRequestScreen(
    navController: NavController,
    viewModel: EditRequestViewModel // Ora ricevuto come parametro dal Navigation
) {
    val request = viewModel.requestFlow.collectAsStateWithLifecycle().value
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val difficulty by viewModel.difficulty.collectAsState()
    val peopleRequired by viewModel.peopleRequired.collectAsState()
    val location by viewModel.location.collectAsState()

    if (request == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Modifica richiesta", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(value = title, onValueChange = viewModel::onTitleChange, label = { Text("Titolo") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = viewModel::onDescriptionChange, label = { Text("Descrizione") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = peopleRequired.toString(),
            onValueChange = { it.toIntOrNull()?.let(viewModel::onPeopleRequiredChange) },
            label = { Text("Numero persone richieste") },
            modifier = Modifier.fillMaxWidth()
        )

        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedTextField(
                value = difficulty,
                onValueChange = {},
                readOnly = true,
                label = { Text("Difficoltà") },
                modifier = Modifier.fillMaxWidth().clickable { expanded = true }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf("Bassa", "Media", "Alta").forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = { viewModel.onDifficultyChange(it); expanded = false })
                }
            }
        }

        OutlinedTextField(value = location, onValueChange = viewModel::onLocationChange, label = { Text("Posizione") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(8.dp))
        Button(onClick = { viewModel.save { navController.popBackStack() } }, modifier = Modifier.align(androidx.compose.ui.Alignment.End)) {
            Text("Salva")
        }
    }
}