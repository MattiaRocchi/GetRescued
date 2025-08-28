package com.example.myapplication.ui.registration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.myapplication.ui.GetRescuedRoute
import com.example.myapplication.ui.composables.AgeTextField
import com.example.myapplication.ui.composables.EmailTextField
import com.example.myapplication.ui.composables.NameTextField
import com.example.myapplication.ui.composables.PasswordTextField
import com.example.myapplication.ui.composables.PhoneTextField
import com.example.myapplication.ui.composables.SurnameTextField
import com.example.myapplication.ui.composables.isValidPhoneNumber
import com.example.myapplication.ui.profile.TagPickerDialog
import kotlinx.coroutines.launch


@Composable
fun RegistrationScreen(
    navController: NavHostController,
    viewModel: RegistrationViewModel
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val allTags by viewModel.allTags.collectAsState() // puoi caricarli via ViewModel
    var showTagsDialog by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var surnameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var ageError by remember { mutableStateOf(false) }
    var phoneNumberError by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(padding)
                .verticalScroll(scrollState)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NameTextField(
                value = viewModel.name,
                onValueChange = {
                    viewModel.onNameChange(it)
                    nameError = it.isBlank()
                },
                nameError = nameError
            )
            Spacer(Modifier.height(8.dp))

            SurnameTextField(
                value = viewModel.surname,
                onValueChange = {
                    viewModel.onSurnameChange(it)
                    surnameError = it.isBlank()
                },
                surnameError = surnameError
            )

            Spacer(Modifier.height(8.dp))



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

            PasswordTextField(
                value = viewModel.password,
                onValueChange = {
                    viewModel.onPasswordChange(it)
                    passwordError = it.length < 6
                },
                passwordError = passwordError
            )

            Spacer(Modifier.height(8.dp))

            AgeTextField(
                value = viewModel.age.toString(),
                onValueChange = {
                    viewModel.onAgeChange(it)
                    ageError = it.toIntOrNull()?.let { n -> n <= 0 } ?: true
                },
                ageError = ageError
            )

            Spacer(Modifier.height(16.dp))

            PhoneTextField(
                value = viewModel.phoneNumber,
                onValueChange = {
                    viewModel.onPhoneNumberChange(it)
                    phoneNumberError = it.isNotBlank() && !isValidPhoneNumber(it)
                },
                phoneError = phoneNumberError
            )
            Button(
                onClick = { showTagsDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Scegli i tuoi interessi (tag)")
            }


            Button(
                onClick = {
                    viewModel.registerUser(
                        onSuccess = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Registrazione avvenuta!")
                            }
                            navController.navigate(GetRescuedRoute.Login)
                        },
                        onError = { msg ->
                            scope.launch {
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    )
                },
                enabled = !nameError && !surnameError && !emailError && !passwordError && !ageError
                            && !phoneNumberError,

                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrati")
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = { navController.navigate(GetRescuedRoute.Login) }) {
                Text("Hai già un account? Accedi")
            }

            if (showTagsDialog) {
                TagPickerDialog(
                    tags = allTags,
                    selectedTagIds = viewModel.selectedTags,
                    onDismiss = { showTagsDialog = false },
                    onConfirm = { selected ->
                        viewModel.onTagsSelected(selected)
                        showTagsDialog = false
                    }
                )
            }
        }
    }
}
