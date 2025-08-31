package com.example.myapplication.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import com.example.myapplication.data.database.UserDao
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val userDao: UserDao
) {

    companion object {
        val LOGGED_IN_USER_ID = intPreferencesKey("logged_in_user_id")
        private val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        private val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        private val CAMERA_ENABLED = booleanPreferencesKey("camera_enabled")
        private val LOCATION_ENABLED = booleanPreferencesKey("location_enabled")

    }


    val musicEnabledFlow: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[MUSIC_ENABLED] ?: false }
    val musicVolumeFlow: Flow<Float> =
        dataStore.data.map { prefs -> prefs[MUSIC_VOLUME] ?: 0.5f }
    val cameraEnabledFlow: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[CAMERA_ENABLED] ?: false }
    val locationEnabledFlow: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[LOCATION_ENABLED] ?: false }


    val userIdFlow: Flow<Int> = dataStore.data.map { prefs ->
        prefs[LOGGED_IN_USER_ID] ?: -1
    }

    val validUserFlow: Flow<Int?> =
        userIdFlow.flatMapLatest { id ->
            if (id <= 0) { // Cambiato da == -1 a <= 0
                flowOf(null)
            } else {
                flow {
                    try {
                        val user = userDao.getById(id)
                        emit(user?.id) // null se non trovato
                    } catch (e: Exception) {
                        emit(null) // In caso di errore, considera l'utente non valido
                    }
                }
            }
        }

    suspend fun setLoggedInUser(
        id: Int,
    ) {
        dataStore.edit { prefs ->
            prefs[LOGGED_IN_USER_ID] = id
        }
    }

    suspend fun logout() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }



    suspend fun setMusicEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[MUSIC_ENABLED] = enabled }
    }

    suspend fun setMusicVolume(volume: Float) {
        // clamp between 0f and 1f
        val v = when {
            volume < 0f -> 0f
            volume > 1f -> 1f
            else -> volume
        }
        dataStore.edit { prefs -> prefs[MUSIC_VOLUME] = v }
    }

    suspend fun setCameraEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[CAMERA_ENABLED] = enabled }
    }

    suspend fun setLocationEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[LOCATION_ENABLED] = enabled }
    }
}
