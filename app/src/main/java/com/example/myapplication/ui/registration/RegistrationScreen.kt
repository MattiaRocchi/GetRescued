package com.example.myapplication.ui.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.time.Instant
import java.time.ZoneId
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Popup
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.core.i18n.DateTimeFormatter
import com.example.myapplication.ui.GetRescuedNavGraph
import com.example.myapplication.ui.GetRescuedRoute
import java.text.SimpleDateFormat
import java.util.*

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
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                cursorColor = MaterialTheme.colorScheme.onPrimary,
                //focusedIndicatorColor = Color.Green,
                //unfocusedIndicatorColor = Color.Gray,
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,   // Sfondo quando attivo
                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer  // Sfondo quando inattivo
            )
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.surname,
            onValueChange = viewModel::onSurnameChange,
            label = { Text("Cognome") },
            modifier = Modifier
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
            cursorColor = MaterialTheme.colorScheme.onPrimary,
            //focusedIndicatorColor = Color.Green,
            //unfocusedIndicatorColor = Color.Gray,
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,   // Sfondo quando attivo
            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer  // Sfondo quando inattivo
        )
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSecondary,
                unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
                cursorColor = MaterialTheme.colorScheme.onSecondary,
                //focusedIndicatorColor = Color.Green,
                //unfocusedIndicatorColor = Color.Gray,
                focusedContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,   // Sfondo quando attivo
                unfocusedContainerColor = MaterialTheme.colorScheme.onSecondaryContainer  // Sfondo quando inattivo
            )
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSecondary,
                unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
                cursorColor = MaterialTheme.colorScheme.onSecondary,
                //focusedIndicatorColor = Color.Green,
                //unfocusedIndicatorColor = Color.Gray,
                focusedContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,   // Sfondo quando attivo
                unfocusedContainerColor = MaterialTheme.colorScheme.onSecondaryContainer  // Sfondo quando inattivo
            )
        )

        Spacer(Modifier.height(8.dp))

        //TODO DatePickerDocked()

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.registerUser(
                    onSuccess = {
                        navController.navigate(GetRescuedRoute.addRequest)  },
                    onError = { errorMsg -> println("Errore: $errorMsg") }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrati")
        }
    }
}

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDocked() {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = { },
            label = { Text("DOB") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = !showDatePicker }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )

        if (showDatePicker) {
            Popup(
                onDismissRequest = { showDatePicker = false },
                alignment = Alignment.TopStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 64.dp)
                        .shadow(elevation = 4.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false
                    )
                }
            }
        }
    }
}

@Composable
fun DatePickerFieldToModal(modifier: Modifier = Modifier) {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var showModal by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate?.let { convertMillisToDate(it) } ?: "",
        onValueChange = { },
        label = { Text("DOB") },
        placeholder = { Text("MM/DD/YYYY") },
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Select date")
        },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(selectedDate) {
                awaitEachGesture {
                    // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
                    // in the Initial pass to observe events before the text field consumes them
                    // in the Main pass.
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        showModal = true
                    }
                }
            }
    )

    if (showModal) {
        DatePickerModal(
            onDateSelected = { selectedDate = it },
            onDismiss = { showModal = false }
        )
    }
}


fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}*/