package com.example.myapplication.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.Tags
import com.example.myapplication.data.database.TitleBadge
import com.example.myapplication.data.database.UserWithInfo
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.TagsRepository
import com.example.myapplication.data.repositories.TitleBadgeRepository
import com.example.myapplication.data.repositories.UserDaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val titleBadgeRepository: TitleBadgeRepository,
    private val userDaoRepository: UserDaoRepository,
    private val settingsRepository: SettingsRepository,
    private val tagsRepository: TagsRepository
) : ViewModel() {

    // UserId dallo store
    val userId = settingsRepository.userIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    // Stato UI
    private val _user = MutableStateFlow<UserWithInfo?>(null)
    val user: StateFlow<UserWithInfo?> = _user

    private val _userTitles = MutableStateFlow<List<TitleBadge>>(emptyList())
    val userTitles: StateFlow<List<TitleBadge>> = _userTitles

    private val _userActiveTitle = MutableStateFlow<TitleBadge?>(null)
    val userActiveTitle: StateFlow<TitleBadge?> = _userActiveTitle

    private val _allTitles = MutableStateFlow<List<TitleBadge>>(emptyList())
    val allTitles: StateFlow<List<TitleBadge>> = _allTitles

    // Tags
    private val _allTags = MutableStateFlow<List<Tags>>(emptyList())
    val allTags: StateFlow<List<Tags>> = _allTags

    private val _userTags = MutableStateFlow<List<Tags>>(emptyList())
    val userTags: StateFlow<List<Tags>> = _userTags

    init {
        // 🔹 ogni volta che cambia userId → ricarico tutto (inclusi tags)
        viewModelScope.launch {
            userId.collect { id ->
                if (id != -1) {
                    // user + titles
                    _user.value = userDaoRepository.getUserWithInfo(id)
                    _userTitles.value = titleBadgeRepository.getUserTitles(id)
                    _userActiveTitle.value = titleBadgeRepository.getActiveTitleByUserId(id)
                    _allTitles.value = titleBadgeRepository.getAll()

                    try {
                        _allTags.value = tagsRepository.getAll()          // <-- List<Tags>
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        _allTags.value = emptyList()
                    }

                    try {
                        _userTags.value = tagsRepository.getTagsForUser(id)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        _userTags.value = emptyList()
                    }
                } else {
                    // Reset se non loggato
                    _user.value = null
                    _userTitles.value = emptyList()
                    _userActiveTitle.value = null
                    _allTitles.value = emptyList()
                    _allTags.value = emptyList()
                    _userTags.value = emptyList()
                }
            }
        }
    }

    // === funzioni esposte per la UI ===

    /**
     * Restituisce l'insieme degli id dei tag attualmente selezionati dall'utente.
     * Utile per passare direttamente a TagPickerDialog(selectedTagIds = ...)
     */
    fun getSelectedTagIds(): Set<Int> =
        _userTags.value.map { it.id }.toSet()

    /**
     * Aggiorna i tag dell'utente: salva i nuovi tag nel DB e ricarica lo stato locale.
     * selectedIds può venire dal TagPickerDialog (Set<Int>).
     */
    fun updateUserTags(selectedIds: Set<Int>) {
        viewModelScope.launch {
            val id = userId.value
            if (id == -1) return@launch

            try {
                // chiama repository per sostituire i tag dell'utente
                tagsRepository.replaceUserTags(id, selectedIds.toList())

                // ricarica i tag dell'utente (e opzionalmente l'utente stesso se contiene info derivate)
                _userTags.value = tagsRepository.getTagsForUser(id)
                _user.value = userDaoRepository.getUserWithInfo(id) // se vuoi aggiornare altre view dipendenti
            } catch (t: Throwable) {
                t.printStackTrace()
                // qui potresti esporre un StateFlow di errori se vuoi mostrare messaggi UI
            }
        }
    }

    // ... resto delle funzioni già presenti ...

    fun updateProfilePhoto(newUri: String) {
        viewModelScope.launch {
            val id = userId.value
            if (id != -1) {
                val success = userDaoRepository.updateProfPic(id, newUri)
                if (success) {
                    _user.value = userDaoRepository.getUserWithInfo(id) // refresh
                }
            }
        }
    }

    fun updateActiveTitle(newTitleId: Int) {
        viewModelScope.launch {
            val id = userId.value
            if (id != -1) {
                titleBadgeRepository.updateActiveTitle(id, newTitleId)
                _user.value = userDaoRepository.getUserWithInfo(id) // refresh user
                _userActiveTitle.value = titleBadgeRepository.getActiveTitleByUserId(id)
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.logout()
            onSuccess()
        }
    }
}
