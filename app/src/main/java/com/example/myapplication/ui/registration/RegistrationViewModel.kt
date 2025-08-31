package com.example.myapplication.ui.registration

import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Tags
import com.example.myapplication.data.database.User
import com.example.myapplication.data.repositories.MissionRepository
import com.example.myapplication.data.repositories.TagsRepository
import com.example.myapplication.data.repositories.TitleBadgeRepository

import com.example.myapplication.data.repositories.UserDaoRepository
import com.example.myapplication.utils.PasswordHasher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val userDaoRepository: UserDaoRepository,
    private val titleBadgeRepository: TitleBadgeRepository,
    private val missionRepository: MissionRepository,
    private val tagsRepository: TagsRepository
) : ViewModel() {

    var selectedTags by mutableStateOf<Set<Int>>(emptySet())
        private set

    fun onTagsSelected(newTags: Set<Int>) {
        selectedTags = newTags
    }
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

    var phoneNumber by mutableStateOf("")
        private set

    private val _allTags = MutableStateFlow<List<Tags>>(emptyList())
    val allTags: StateFlow<List<Tags>> = _allTags
    init {
        //ogni volta che cambia userId → ricarico tutto (inclusi tags)
        viewModelScope.launch {
                    try {
                        _allTags.value = tagsRepository.getAll()
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        _allTags.value = emptyList()
                    }

        }
    }



    fun onNameChange(newValue: String) { name = newValue }
    fun onSurnameChange(newValue: String) { surname = newValue }
    fun onEmailChange(newValue: String) { email = newValue }
    fun onPasswordChange(newValue: String) { password = newValue }
    fun onAgeChange(newValue: String) {
        age = newValue.toIntOrNull() ?: 0 // se non valido, torna 0
    }
    fun onPhoneNumberChange(newValue: String) {
        phoneNumber = newValue // se non valido, torna 0
    }

    fun registerUser(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                if (name.isBlank() || surname.isBlank() || email.isBlank()
                    || password.isBlank() || age == 0) {
                    onError("Compila tutti i campi obbligatori")
                    return@launch
                }
                val hashedPassword = PasswordHasher.hash(password)
                val newUser = User(
                    name = name,
                    surname = surname,
                    email = email,
                    password = hashedPassword,
                    age = age,
                    phoneNumber = phoneNumber

                )
                val id = userDaoRepository.insertUserWithInfo(newUser).toInt()
                titleBadgeRepository.insertUserBadgeCrossRef(id, 0)
                titleBadgeRepository.updateActiveTitle(id, 0)

                tagsRepository.replaceUserTags(id, selectedTags.toList())

                missionRepository.setGeneralMissionsUser(id)
                missionRepository.setWeeklyMissionsUser(id)

                onSuccess()

            } catch (e: SQLiteConstraintException) {
                onError("Questa email è già registrata:")
            } catch (e: Exception) {
                onError("Errore durante la registrazione: ${e.message}")
            }
        }
    }
}
