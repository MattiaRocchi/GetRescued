package com.example.myapplication.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.myapplication.data.database.UserDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

//TODO da snellire
class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val userDao: UserDao
) {
    companion object {
        val LOGGED_IN_USER_ID = intPreferencesKey("logged_in_user_id")
        val LOGGED_IN_EMAIL = stringPreferencesKey("logged_in_email")
        val LOGGED_IN_NAME = stringPreferencesKey("logged_in_name")
        val LOGGED_IN_SURNAME = stringPreferencesKey("logged_in_surname")
        val LOGGED_IN_ACTIVE_TITLE = intPreferencesKey("logged_in_title_badge")

    }



    val userIdFlow: Flow<Int> = dataStore.data.map { prefs ->
        prefs[LOGGED_IN_USER_ID] ?: -1
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val validUserFlow: Flow<Int?> =
        userIdFlow.flatMapLatest { id ->
            if (id == -1) {
                flowOf(null)
            } else {
                flow {
                    val user = userDao.getById(id)
                    emit(user?.id) // null se non trovato
                }
            }
        }

    val emailFlow: Flow<String?> = dataStore.data.map { prefs ->
        prefs[LOGGED_IN_EMAIL]
    }

    val nameFlow: Flow<String?> = dataStore.data.map { prefs ->
        prefs[LOGGED_IN_NAME]
    }

    val surnameFlow: Flow<String?> = dataStore.data.map { prefs ->
        prefs[LOGGED_IN_SURNAME]
    }
    val activeTitleBadge: Flow<Int> = dataStore.data.map { prefs ->
        prefs[LOGGED_IN_ACTIVE_TITLE] ?: 0
    }

    suspend fun updateActiveTitle(activeTitle: Int) {
        dataStore.edit { prefs ->
            prefs[LOGGED_IN_ACTIVE_TITLE] = activeTitle
        }
    }
    suspend fun setLoggedInUser(
        id: Int,
        email: String,
        name: String,
        surname: String,
        activeTitle: Int
    ) {
        dataStore.edit { prefs ->
            prefs[LOGGED_IN_USER_ID] = id
            prefs[LOGGED_IN_EMAIL] = email
            prefs[LOGGED_IN_NAME] = name
            prefs[LOGGED_IN_SURNAME] = surname
            prefs[LOGGED_IN_ACTIVE_TITLE] = activeTitle
        }
    }

    suspend fun logout() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
