package com.example.myapplication.ui.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.myapplication.ui.theme.PrimaryContainer
import com.example.myapplication.ui.theme.SecondaryContainer
import com.example.myapplication.ui.theme.TertiaryContainer
import java.time.LocalDate

@Composable
fun RegistrationScreen(
    navController: NavHostController,
    viewModel: RegistrationViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = viewModel.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Nome") },
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryContainer, RoundedCornerShape(8.dp))
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.surname,
            onValueChange = viewModel::onSurnameChange,
            label = { Text("Cognome") },
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryContainer, RoundedCornerShape(8.dp))
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .background(SecondaryContainer, RoundedCornerShape(8.dp))
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .background(SecondaryContainer, RoundedCornerShape(8.dp))
        )

        Spacer(Modifier.height(8.dp))

        DatePickerField(
            selectedDate = viewModel.birthDate,
            onDateSelected = viewModel::onBirthDateChange
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.registerUser(
                    onSuccess = { /* navController.navigate(...) */ },
                    onError = { errorMsg -> println("Errore: $errorMsg") }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrati")
        }
    }
}

@Composable
fun DatePickerField(selectedDate: LocalDate?, onDateSelected: (LocalDate) -> Unit) {
    OutlinedTextField(
        value = selectedDate?.toString() ?: "",
        onValueChange = {},
        label = { Text("Data di Nascita") },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .background(TertiaryContainer, RoundedCornerShape(8.dp))
    )
}
