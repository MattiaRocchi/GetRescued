package com.example.myapplication.ui.changeProfile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.User
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.UserDaoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChangeProfileViewModel(
    private val userDaoRepository: UserDaoRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val userId = settingsRepository.userIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    // campi di input per la UI
    var name by mutableStateOf("")
    var surname by mutableStateOf("")
    var email by mutableStateOf("")
    var ageText by mutableStateOf("")
    var habitation by mutableStateOf<String?>(null)
    var phoneNumber by mutableStateOf<String?>(null)

    // stato
    var isLoading by mutableStateOf(true)
        private set
    var isSaving by mutableStateOf(false)
        private set

    // tieni una copia dell'user originale per non perdere campi non mostrati/modificati
    private var originalUser: User? = null

    init {
        viewModelScope.launch { loadCurrentUser() }
    }

    private suspend fun loadCurrentUser() {
        isLoading = true
        try {
            val id = userId.first()
            if (id != -1) {
                val u = userDaoRepository.getById(id) // deve restituire User?
                u?.let { user ->
                    originalUser = user
                    name = user.name
                    surname = user.surname
                    email = user.email
                    ageText = user.age.toString()
                    habitation = user.habitation
                    phoneNumber = user.phoneNumber
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    fun updateProfile(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val id = userId.value
            if (id == -1) {
                onError("Utente non loggato")
                return@launch
            }

            if (name.isBlank()) { onError("Inserisci il nome"); return@launch }
            if (surname.isBlank()) { onError("Inserisci il cognome"); return@launch }
            val age = ageText.toIntOrNull()
            if (age == null || age <= 0) { onError("Inserisci un'età valida"); return@launch }

            // evita sovrascritture: prendi password/createdAt dall'originalUser se esiste
            val orig = originalUser
            if (orig == null) {
                onError("Impossibile leggere i dati utente")
                return@launch
            }

            isSaving = true
            try {
                val updatedUser = User(
                    id = orig.id,
                    name = name.trim(),
                    surname = surname.trim(),
                    email = email.trim(),
                    password = orig.password,          // mantieni la password se non la modifichi qui
                    age = age,
                    habitation = habitation?.trim(),
                    phoneNumber = phoneNumber?.trim(),
                    createdAt = orig.createdAt         // mantieni createdAt
                )

                // il tuo repository ha un updateUser(user) — lo chiamiamo
                userDaoRepository.updateUser(updatedUser)

                // ricarica l'originalUser (opzionale)
                originalUser = updatedUser
                loadCurrentUser()

                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                // se Room lancia SQLiteConstraintException per email unique -> catturalo e traduci messaggio
                onError(e.message ?: "Errore durante il salvataggio")
            } finally {
                isSaving = false
            }
        }
    }
}
