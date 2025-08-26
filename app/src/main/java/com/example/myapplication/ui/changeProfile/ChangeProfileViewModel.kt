package com.example.myapplication.ui.changeProfile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.User
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.UserDaoRepository
import com.example.myapplication.utils.PasswordHasher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChangeProfileViewModel(
    private val userDaoRepository: UserDaoRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // id utente corrente, null se non loggato
    private val _currentUserId = MutableStateFlow<Int?>(null)
    val userId: StateFlow<Int?> = _currentUserId.asStateFlow()

    // campi UI
    var name by mutableStateOf("")
        private set
    var surname by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var ageText by mutableStateOf("")
        private set
    var habitation by mutableStateOf<String?>(null)
        private set
    var phoneNumber by mutableStateOf<String?>(null)
        private set

    // password
    var newPassword by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set

    // stato
    var isLoading by mutableStateOf(true)
        private set
    var isSaving by mutableStateOf(false)
        private set

    private var originalUser: User? = null

    init {
        // ascolta sempre l'id utente valido dal repo
        viewModelScope.launch {
            settingsRepository.validUserFlow.collect { id ->
                _currentUserId.value = id
                if (id != null) {
                    loadCurrentUser(id)
                } else {
                    resetFields()
                }
            }
        }
    }

    private fun resetFields() {
        originalUser = null
        name = ""
        surname = ""
        email = ""
        ageText = ""
        habitation = null
        phoneNumber = null
        newPassword = ""
        confirmPassword = ""
        isLoading = false
    }

    private suspend fun loadCurrentUser(id: Int) {
        isLoading = true
        try {
            val u = userDaoRepository.getById(id)
            if (u != null) {
                originalUser = u
                name = u.name
                surname = u.surname
                email = u.email
                ageText = u.age.toString()
                habitation = u.habitation
                phoneNumber = u.phoneNumber
            } else {
                resetFields()
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            resetFields()
        } finally {
            isLoading = false
        }
    }

    // onChange helpers
    fun onNameChange(v: String) { name = v }
    fun onSurnameChange(v: String) { surname = v }
    fun onEmailChange(v: String) { email = v }
    fun onAgeTextChange(v: String) { ageText = v }
    fun onHabitationChange(v: String?) { habitation = v }
    fun onPhoneNumberChange(v: String?) { phoneNumber = v }
    fun onNewPasswordChange(v: String) { newPassword = v }
    fun onConfirmPasswordChange(v: String) { confirmPassword = v }

    fun updateProfile(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val id = userId.value
            if (id == null) { onError("Utente non loggato"); return@launch }

            // validazioni
            if (name.isBlank()) { onError("Inserisci il nome"); return@launch }
            if (surname.isBlank()) { onError("Inserisci il cognome"); return@launch }
            val age = ageText.toIntOrNull()
            if (age == null || age <= 0) { onError("Inserisci un'età valida"); return@launch }

            val wantChangePassword = newPassword.isNotBlank() || confirmPassword.isNotBlank()
            if (wantChangePassword) {
                if (newPassword.length < 6) { onError("La password deve avere almeno 6 caratteri"); return@launch }
                if (newPassword != confirmPassword) { onError("Le password non corrispondono"); return@launch }
            }

            val orig = originalUser
            if (orig == null) { onError("Impossibile leggere i dati utente"); return@launch }

            isSaving = true
            try {
                if (email.trim() == orig.email.trim()) {
                    // update standard
                    val updatedUser = orig.copy(
                        name = name.trim(),
                        surname = surname.trim(),
                        email = email.trim(),
                        password = if (wantChangePassword) PasswordHasher.hash(newPassword) else orig.password,
                        age = age,
                        habitation = habitation?.trim(),
                        phoneNumber = phoneNumber?.trim()
                    )
                    userDaoRepository.updateUser(updatedUser)
                    originalUser = updatedUser
                    loadCurrentUser(orig.id)
                    onSuccess()
                } else {
                    // ==== EMAIL CHANGED FLOW ====
                    val newUser = orig.copy(
                        id = 0, // nuovo record
                        name = name.trim(),
                        surname = surname.trim(),
                        email = email.trim(),
                        password = if (wantChangePassword) PasswordHasher.hash(newPassword) else orig.password,
                        age = age,
                        habitation = habitation?.trim(),
                        phoneNumber = phoneNumber?.trim()
                    )

                    val newIdLong = userDaoRepository.insertUserWithInfo(newUser)
                    val newId = newIdLong.toInt()

                    // aggiorna sessione
                    settingsRepository.setLoggedInUser(newId)

                    // eventuale migrazione dati vecchio utente
                    userDaoRepository.insertUserWithInfoChange(newUser, orig.id)

                    loadCurrentUser(newId)
                    onSuccess()
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                onError(t.message ?: "Errore durante il salvataggio")
            } finally {
                isSaving = false
            }
        }
    }
}
