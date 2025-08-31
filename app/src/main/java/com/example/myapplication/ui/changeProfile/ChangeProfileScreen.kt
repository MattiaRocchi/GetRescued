package com.example.myapplication.ui.changeProfile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import com.example.myapplication.ui.composables.*

@Composable
fun ChangeProfileScreen(
    navController: NavHostController,
    viewModel: ChangeProfileViewModel
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // bind ai campi del VM
    val name by remember { derivedStateOf { viewModel.name } }
    val surname by remember { derivedStateOf { viewModel.surname } }
    val ageText by remember { derivedStateOf { viewModel.ageText } }
    val habitation by remember { derivedStateOf { viewModel.habitation } }
    val phone by remember { derivedStateOf { viewModel.phoneNumber } }
    val newPassword by remember { derivedStateOf { viewModel.newPassword } }
    val confirmPassword by remember { derivedStateOf { viewModel.confirmPassword } }

    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    val isSaving by remember { derivedStateOf { viewModel.isSaving } }

    var nameError by remember { mutableStateOf(false) }
    var surnameError by remember { mutableStateOf(false) }
    var ageError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var phoneNumberError by remember { mutableStateOf(false) }
    var confirmPassError by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(padding)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileTextField(value = name, onValueChange = {
                viewModel.onNameChange(it); nameError = it.isBlank()
            }, label = "Nome")

            Spacer(Modifier.height(8.dp))

            ProfileTextField(value = surname, onValueChange = {
                viewModel.onSurnameChange(it); surnameError = it.isBlank()
            }, label = "Cognome")

            Spacer(Modifier.height(8.dp))

            ProfileTextField(value = ageText, onValueChange = {
                viewModel.onAgeTextChange(it); ageError = it.toIntOrNull()?.let { n -> n <= 0 } ?: true
            }, label = "Età")

            if (ageError) Text("Inserisci un'età valida", color = MaterialTheme.colorScheme.error)

            Spacer(Modifier.height(8.dp))

            ProfileTextField(
                value = habitation ?: "",
                onValueChange = { viewModel.onHabitationChange(it) },
                label = "Residenza (facoltativo)"
            )

            Spacer(Modifier.height(8.dp))

            PhoneTextField(
                value = phone ?: "",
                onValueChange = {
                    viewModel.onPhoneNumberChange(it)
                    phoneNumberError = it.isNotBlank() && !isValidPhoneNumber(it)
                },
                phoneError = phoneNumberError
            )

            Spacer(Modifier.height(12.dp))

            Text("Cambia password", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            // Password + confirm
            PasswordTextField(
                value = newPassword,
                onValueChange = {
                    viewModel.onNewPasswordChange(it)
                    passwordError =  it.isNotBlank() && !isValidPhoneNumber(it)
                },
                passwordError = passwordError,
                label = "Nuova password"
            )
            Spacer(Modifier.height(8.dp))
            PasswordTextField(
                value = confirmPassword,
                onValueChange = {
                    viewModel.onConfirmPasswordChange(it)
                    confirmPassError = (newPassword.isNotBlank() && it != newPassword)
                },
                passwordError = confirmPassError,
                label = "Conferma password"
            )
            if (confirmPassError) {
                Text("Le password non corrispondono", color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.updateProfile(onSuccess = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Profilo aggiornato")
                            navController.popBackStack()
                        }
                    }, onError = { msg ->
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    })
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
            TextButton(onClick = { navController.popBackStack() }) { Text("Annulla") }
        }
    }
}
