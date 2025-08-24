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


class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val userDao: UserDao
) {
    companion object {
        val LOGGED_IN_USER_ID = intPreferencesKey("logged_in_user_id")

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
}
