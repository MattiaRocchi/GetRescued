package com.example.myapplication.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.UserDaoRepository
import kotlinx.coroutines.launch

//TODO
class LoginViewModel(
    private val userDaoRepository: UserDaoRepository,
    private val settingsRepository: SettingsRepository

): ViewModel() {
    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    fun onEmailChange(newValue: String) { email = newValue}
    fun onPasswordChange(newValue: String) {password = newValue}

    fun loginUser(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                if (email.isBlank() || password.isBlank()) {
                    onError("Compila tutti i campi obbligatori")
                    return@launch
                }


                val user = userDaoRepository.getByEmail(email)

                when {
                    user == null -> {
                        onError("Mail non registrata")
                    }
                    user.password != password -> {
                        onError("Password errata")
                    }
                    else -> {
                        settingsRepository.setLoggedInUser(
                            user.id,
                            user.email,
                            user.name,
                            user.surname,
                            0)
                        onSuccess()
                    }
                }


            } catch (e: Exception) {
                onError("Errore durante il login: ${e.message}")
            }
        }
    }




}