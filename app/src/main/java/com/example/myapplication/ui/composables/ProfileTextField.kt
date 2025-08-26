package com.example.myapplication.ui.composables

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlin.String

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        enabled = enabled
    )
}

@Composable
fun EmailTextField(
    value: String,
    onValueChange: (String) -> Unit,
    emailError: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text("Email") },
        isError = emailError,
        modifier = modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            focusedTextColor = MaterialTheme.colorScheme.onSecondary,
            unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
            cursorColor = MaterialTheme.colorScheme.onSecondary
        )
    )
    if (emailError) {
        Text("Inserisci un'email valida", color = MaterialTheme.colorScheme.error)
    }
}


@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    passwordError: Boolean,
    modifier: Modifier = Modifier,
    label: String = "Password"
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(label) },
        isError = passwordError,
        modifier = modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            focusedTextColor = MaterialTheme.colorScheme.onSecondary,
            unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
            cursorColor = MaterialTheme.colorScheme.onSecondary
        ),
        visualTransformation = if (passwordVisible) VisualTransformation.None else
            PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (passwordVisible) Icons.Default.Visibility else
                Icons.Default.VisibilityOff
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription =
                    if (passwordVisible) "Nascondi password" else "Mostra password")
            }
        }
    )

    if (passwordError) {
        Text("La password deve avere almeno 6 caratteri", color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun NameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    nameError: Boolean,
    modifier: Modifier = Modifier
){
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text("Nome") },
        isError = nameError,
        modifier = modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
            cursorColor = MaterialTheme.colorScheme.onPrimary
        )
    )
    if (nameError) Text("Inserisci il nome", color = MaterialTheme.colorScheme.error)

}

// 🔹 COGNOME
@Composable
fun SurnameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    surnameError: Boolean,
    modifier: Modifier = Modifier
) {

    OutlinedTextField(
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        label = { Text("Cognome") },
        isError = surnameError,
        modifier = modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
            cursorColor = MaterialTheme.colorScheme.onPrimary
        )
    )
    if (surnameError) Text("Inserisci il cognome", color = MaterialTheme.colorScheme.error)
}
// 🔹 ETÀ
@Composable
fun AgeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    ageError: Boolean,
    modifier: Modifier = Modifier
){
    OutlinedTextField(
        value = value,
        onValueChange = {
            onValueChange(it)

        },
        label = { Text("Età") },
        isError = ageError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            focusedTextColor = MaterialTheme.colorScheme.onTertiary,
            unfocusedTextColor = MaterialTheme.colorScheme.onTertiary,
            cursorColor = MaterialTheme.colorScheme.onTertiary
        )
    )
    if (ageError) Text("Inserisci un'età valida", color = MaterialTheme.colorScheme.error)
}

@Composable
fun HabitationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    habitationError: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text("Residenza (facoltativo)") },
        isError = habitationError,
        modifier = modifier.fillMaxWidth(),
        singleLine = false,
        maxLines = 2,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
            cursorColor = MaterialTheme.colorScheme.onPrimary
        )
    )
    if (habitationError) {
        Spacer(modifier = Modifier.height(4.dp))
        Text("Inserisci un indirizzo valido", color = MaterialTheme.colorScheme.error)
    }
}

// -----------------------------
// Phone number
// -----------------------------
@Composable
fun PhoneTextField(
    value: String,
    onValueChange: (String) -> Unit,
    phoneError: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text("Telefono (facoltativo)") },
        isError = phoneError,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
            cursorColor = MaterialTheme.colorScheme.onPrimary
        )
    )
    if (phoneError) {
        Spacer(modifier = Modifier.height(4.dp))
        Text("Inserisci un numero di telefono valido", color = MaterialTheme.colorScheme.error)
    }
}