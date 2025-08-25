package com.example.myapplication.ui.login

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.myapplication.ui.GetRescuedRoute
import com.example.myapplication.ui.composables.EmailTextField
import com.example.myapplication.ui.composables.PasswordTextField
import com.example.myapplication.utils.MusicService
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(16.dp),
        ) {

            // Campo Email
            EmailTextField(
                value = viewModel.email,
                onValueChange = {
                    viewModel.onEmailChange(it)
                    emailError = it.isBlank() ||
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
                },
                emailError = emailError
            )

            Spacer(Modifier.height(8.dp))

            // Campo Password
            PasswordTextField(
                value = viewModel.password,
                onValueChange = {
                    viewModel.onPasswordChange(it)
                    passwordError = it.length < 6
                },
                passwordError = passwordError
            )

            Spacer(Modifier.height(24.dp))

            // Bottone Accedi
            Button(
                onClick = {
                    viewModel.loginUser(
                        onSuccess = {
                            scope.launch {
                                snackbarHostState
                                    .showSnackbar("Login avvenuto con successo!")
                            }
                            val musicIntent =
                                Intent(context, MusicService::class.java)
                            context.startService(musicIntent)
                            navController.navigate(GetRescuedRoute.Profile) {
                                popUpTo(GetRescuedRoute.Login) { inclusive = true }
                            }
                        },
                        onError = { msg ->
                            scope.launch {
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    )
                },
                enabled = !emailError && !passwordError,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Accedi")
            }

            // Link alla registrazione
            TextButton(onClick = {
                navController.navigate(GetRescuedRoute.Registration)
            }) {
                Text("Sei nuovo? Registrati")
            }
        }
    }
}
