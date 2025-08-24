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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChangeProfileViewModel(
    private val userDaoRepository: UserDaoRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val userId = settingsRepository.userIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), -1)

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

    // password (nuovi campi)
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
        viewModelScope.launch {
            settingsRepository.userIdFlow.collect { id ->
                if (id != -1) loadCurrentUser(id) else resetFields()
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
            } else resetFields()
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

    /**
     * updateProfile:
     * - se e-mail invariata -> semplice updateUser
     * - se e-mail cambiata -> opzione (ricreare user) (vedi commenti sotto)
     *
     * onSuccess/onError come callback.
     */
    fun updateProfile(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val id = userId.first()
            if (id == -1) { onError("Utente non loggato"); return@launch }

            // validazioni
            if (name.isBlank()) { onError("Inserisci il nome"); return@launch }
            if (surname.isBlank()) { onError("Inserisci il cognome"); return@launch }
            val age = ageText.toIntOrNull()
            if (age == null || age <= 0) { onError("Inserisci un'età valida"); return@launch }

            // se cambia password -> valida
            val wantChangePassword = newPassword.isNotBlank() || confirmPassword.isNotBlank()
            if (wantChangePassword) {
                if (newPassword.length < 6) { onError("La password deve avere almeno 6 caratteri"); return@launch }
                if (newPassword != confirmPassword) { onError("Le password non corrispondono"); return@launch }
            }

            val orig = originalUser
            if (orig == null) { onError("Impossibile leggere i dati utente"); return@launch }

            isSaving = true
            try {
                // Se la mail è rimasta uguale -> update standard
                if (email.trim() == orig.email.trim()) {
                    val updatedUser = User(
                        id = orig.id,
                        name = name.trim(),
                        surname = surname.trim(),
                        email = email.trim(),
                        password = if (wantChangePassword) PasswordHasher.hash(newPassword)
                                    else orig.password,
                        age = age,
                        habitation = habitation?.trim(),
                        phoneNumber = phoneNumber?.trim(),
                        createdAt = orig.createdAt
                    )

                    userDaoRepository.updateUser(updatedUser)
                    // aggiorna copia e reload
                    originalUser = updatedUser
                    loadCurrentUser(orig.id)
                    onSuccess()
                } else {
                    // ==== EMAIL CHANGED FLOW ====
                    // Due opzioni:
                    // 1) **Consigliato**: provare ad aggiornare l'email con updateUser (se la unique constraint lo permette).
                    //    Se la tua schema impone email unica in DB, updateUser provvederà (o fallirà con constraint).
                    // 2) **Ricreare** l'utente: inserire nuovo User (con stessa UserInfo), aggiornare sessione (settingsRepository)
                    //    e cancellare il vecchio user. ATTENZIONE: questo può rompere FK riferimenti ad es. request/mission ecc.
                    //
                    // Qui ti mostro la soluzione che *ricrea l'utente* (se preferisci usare updateUser semplice, sostituisci con updateUser).

                    // crea nuovo user usando i dati + password (nuova se impostata o la vecchia)
                    val newUser = User(
                        name = name.trim(),
                        surname = surname.trim(),
                        email = email.trim(),
                        password = if (wantChangePassword) PasswordHasher.hash(newPassword) else orig.password,
                        age = age,
                        habitation = habitation?.trim(),
                        phoneNumber = phoneNumber?.trim(),
                        createdAt = orig.createdAt
                    )
                    // repository: supponiamo ci sia insertUserWithInfo che ritorna Long id (adattare se diverso)
                    val newIdLong = userDaoRepository.insertUserWithInfo(newUser) // **suspend** che ritorna Long
                    val newId = newIdLong.toInt()

                    // cancella utente vecchio (opzionale, attento)
                    userDaoRepository.insertUserWithInfoChange(newUser,orig.id)

                    // aggiorna sessione: qui assumo che SettingsRepository abbia setUserId(Int)
                    // se il metodo ha nome diverso adattalo
                    settingsRepository.setLoggedInUser(newId)

                    // ricarica dati
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
