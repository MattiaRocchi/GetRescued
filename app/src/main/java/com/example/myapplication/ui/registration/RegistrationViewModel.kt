package com.example.myapplication.ui.registration

import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.User
import com.example.myapplication.data.repositories.TitleBadgeRepository

import com.example.myapplication.data.repositories.UserDaoRepository
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val userDaoRepository: UserDaoRepository,
    private val titleBadgeRepository: TitleBadgeRepository
) : ViewModel() {

    var name by mutableStateOf("")
        private set

    var surname by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var age by mutableIntStateOf(0) // non nullable, inizializzato a 0
        private set

    fun onNameChange(newValue: String) { name = newValue }
    fun onSurnameChange(newValue: String) { surname = newValue }
    fun onEmailChange(newValue: String) { email = newValue }
    fun onPasswordChange(newValue: String) { password = newValue }
    fun onAgeChange(newValue: String) {
        age = newValue.toIntOrNull() ?: 0 // se non valido, torna 0
    }


    // Stato per errore età
    var ageError by mutableStateOf(false)
        private set

    fun registerUser(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                if (name.isBlank() || surname.isBlank() || email.isBlank()
                    || password.isBlank() || age == 0) {
                    onError("Compila tutti i campi obbligatori")
                    return@launch
                }

                val newUser = User(
                    name = name,
                    surname = surname,
                    email = email,
                    password = password,
                    age = age
                )
                val id = userDaoRepository.insertUserWithInfo(newUser).toInt()
                titleBadgeRepository.insertUserBadgeCrossRef(id, 0)
                titleBadgeRepository.updateActiveTitle(id, 0)

                onSuccess()

            } catch (e: SQLiteConstraintException) {
                onError("Questa email è già registrata")
            } catch (e: Exception) {
                onError("Errore durante la registrazione: ${e.message}")
            }
        }
    }
}
