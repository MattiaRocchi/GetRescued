package com.example.myapplication.ui.registration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalDate

class RegistrationViewModel : ViewModel() {

    // 🔹 Variabili osservabili
    var name by mutableStateOf("")
        private set

    var surname by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var birthDate by mutableStateOf<LocalDate?>(null)
        private set

    // 🔹 Funzioni per modificare lo stato
    fun onNameChange(newValue: String) { name = newValue }
    fun onSurnameChange(newValue: String) { surname = newValue }
    fun onEmailChange(newValue: String) { email = newValue }
    fun onPasswordChange(newValue: String) { password = newValue }
    fun onBirthDateChange(newDate: LocalDate) { birthDate = newDate }

    // 🔹 Funzione di registrazione
    fun registerUser(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                onError("Compila tutti i campi obbligatori")
                return@launch
            }

            // Qui in futuro potrai chiamare un repository, esempio:
            // userRepository.insertUser(User(...))

            onSuccess()
        }
    }
}
