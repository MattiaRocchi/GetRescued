package com.example.myapplication.ui.changeProfile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@Composable
fun ChangeProfileScreen(
    navController: NavHostController,
    viewModel: ChangeProfileViewModel
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // bind ai campi del VM
    val name by remember { derivedStateOf { viewModel.name } }
    val surname by remember { derivedStateOf { viewModel.surname } }
    val email by remember { derivedStateOf { viewModel.email } }
    val ageText by remember { derivedStateOf { viewModel.ageText } }
    val habitation by remember { derivedStateOf { viewModel.habitation } }
    val phoneNumber by remember { derivedStateOf { viewModel.phoneNumber } }

    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    val isSaving by remember { derivedStateOf { viewModel.isSaving } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.name = it },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = surname,
                onValueChange = { viewModel.surname = it },
                label = { Text("Cognome") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = false // se non vuoi permettere la modifica metti false
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = ageText,
                onValueChange = { viewModel.ageText = it },
                label = { Text("Età") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = habitation ?: "",
                onValueChange = { viewModel.habitation = it },
                label = { Text("Residenza (facoltativo)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = phoneNumber ?: "",
                onValueChange = { viewModel.phoneNumber = it },
                label = { Text("Telefono (facoltativo)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.updateProfile(
                        onSuccess = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Profilo aggiornato")
                                // torna alla schermata profilo
                                navController.popBackStack()
                            }
                        },
                        onError = { msg ->
                            scope.launch {
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    )
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Salvataggio...")
                } else {
                    Text("Salva")
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text("Annulla")
            }
        }
    }
}