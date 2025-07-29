package com.example.myapplication.ui.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.Serializable
import androidx.datastore.preferences.core.Preferences




class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val LOGGED_IN_USER_ID = intPreferencesKey("logged_in_user_id")
    }

    //se nessuno utente è loggato userIdFlow è -1
    val userIdFlow = dataStore.data.map { prefs ->
        prefs[LOGGED_IN_USER_ID] ?: -1
    }

    suspend fun setLoggedInUserId(id: Int) {
        dataStore.edit { prefs ->
            prefs[LOGGED_IN_USER_ID] = id
        }
    }

    suspend fun logout() {
        dataStore.edit { it.remove(LOGGED_IN_USER_ID) }
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