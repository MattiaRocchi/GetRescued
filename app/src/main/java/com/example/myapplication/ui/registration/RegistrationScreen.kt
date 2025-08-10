package com.example.myapplication.ui.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.myapplication.ui.theme.OnPrimary
import com.example.myapplication.ui.theme.OnPrimaryContainer
import com.example.myapplication.ui.theme.OnSecondaryContainer
import com.example.myapplication.ui.theme.OnTertiaryContainer
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
            colors = TextFieldDefaults.colors(
                focusedTextColor = OnPrimaryContainer,        // Colore testo quando attivo
                unfocusedTextColor = OnPrimaryContainer,      // Colore testo quando inattivo
                cursorColor = OnPrimaryContainer,             // Colore cursore
                //focusedIndicatorColor = Color.Green, // Bordo quando attivo
                unfocusedIndicatorColor = Color.Gray, // Bordo quando inattivo
                focusedContainerColor = PrimaryContainer,   // Sfondo quando attivo
                unfocusedContainerColor = PrimaryContainer  // Sfondo quando inattivo
            ),
            modifier = Modifier
                .fillMaxWidth()

        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.surname,
            onValueChange = viewModel::onSurnameChange,
            label = { Text("Cognome") },
            modifier = Modifier
                .fillMaxWidth(),

            colors = TextFieldDefaults.colors(
            focusedTextColor = OnPrimaryContainer,        // Colore testo quando attivo
            unfocusedTextColor = OnPrimaryContainer,      // Colore testo quando inattivo
            cursorColor = OnPrimaryContainer,             // Colore cursore
            //focusedIndicatorColor = Color.Green, // Bordo quando attivo
            unfocusedIndicatorColor = Color.Gray, // Bordo quando inattivo
            focusedContainerColor = PrimaryContainer,   // Sfondo quando attivo
            unfocusedContainerColor = PrimaryContainer  // Sfondo quando inattivo
        ),
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = OnSecondaryContainer,        // Colore testo quando attivo
                unfocusedTextColor = OnSecondaryContainer,      // Colore testo quando inattivo
                cursorColor = OnSecondaryContainer,             // Colore cursore
                //focusedIndicatorColor = Color.Green, // Bordo quando attivo
                unfocusedIndicatorColor = Color.Gray, // Bordo quando inattivo
                focusedContainerColor = SecondaryContainer,   // Sfondo quando attivo
                unfocusedContainerColor = SecondaryContainer  // Sfondo quando inattivo
            ),
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth(),

            colors = TextFieldDefaults.colors(
                focusedTextColor = OnSecondaryContainer,        // Colore testo quando attivo
                unfocusedTextColor = OnSecondaryContainer,      // Colore testo quando inattivo
                cursorColor = OnSecondaryContainer,             // Colore cursore
                //focusedIndicatorColor = Color.Green, // Bordo quando attivo
                unfocusedIndicatorColor = Color.Gray, // Bordo quando inattivo
                focusedContainerColor = SecondaryContainer,   // Sfondo quando attivo
                unfocusedContainerColor = SecondaryContainer  // Sfondo quando inattivo
            ),
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
            .fillMaxWidth(),

        colors = TextFieldDefaults.colors(
            focusedTextColor = OnTertiaryContainer,        // Colore testo quando attivo
            unfocusedTextColor = OnTertiaryContainer,      // Colore testo quando inattivo
            cursorColor = OnTertiaryContainer,             // Colore cursore
            //focusedIndicatorColor = Color.Green, // Bordo quando attivo
            unfocusedIndicatorColor = Color.Gray, // Bordo quando inattivo
            focusedContainerColor = TertiaryContainer,   // Sfondo quando attivo
            unfocusedContainerColor = TertiaryContainer  // Sfondo quando inattivo
    ),
    )
}
