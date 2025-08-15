package com.example.myapplication.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.Serializable
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey


class SettingsRepository(
    private val dataStore: DataStore<Preferences>
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



/*
class UserDSRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val EMAIL =
            stringPreferencesKey("userDS")
    }

    val email = dataStore.data.map { preferences ->
        preferences[EMAIL]
    }

    suspend fun setUser(email: String) =
        dataStore.edit { it[EMAIL] = email }

    suspend fun clearEmail() {
        dataStore.edit { it.remove(EMAIL) }
    }

    suspend fun incrementScore(email: String, points: Int) {
        try {
            val user = supabase.from("users")
                .select() {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeList<UserServer>()

            val newScore = user[0].points + points
            supabase.from("users")
                .update(mapOf("points" to newScore)) {
                    filter {
                        eq("email", email)
                    }
                }
        } catch (e: Exception) {
            Log.e("Supabase", "Error incrementing score: ${e.message}")
        }
    }
}*/