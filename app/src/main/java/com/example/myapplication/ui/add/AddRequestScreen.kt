package com.example.myapplication.ui.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AddRequestScreen(
    navController: NavController,
    viewModel: AddRequestViewModel,
    userId: Int // Lo passi dal chiamante, es. dal MainActivity
) {
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val peopleRequired by viewModel.peopleRequired.collectAsState()
    val difficulty by viewModel.difficulty.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Crea una nuova richiesta", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = title,
            onValueChange = viewModel::onTitleChange,
            label = { Text("Titolo") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text("Descrizione") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = peopleRequired.toString(),
            onValueChange = { it.toIntOrNull()?.let(viewModel::onPeopleRequiredChange) },
            label = { Text("Numero persone richieste") },
            modifier = Modifier.fillMaxWidth()
        )

        // Dropdown per difficoltà
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
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            viewModel.onDifficultyChange(it)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.submitRequest(userId) {
                    navController.popBackStack() // Torna indietro dopo l’inserimento
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Invia richiesta")
        }
    }
}