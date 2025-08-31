package com.example.myapplication.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.MissionRepository
import kotlinx.coroutines.flow.first


class WeeklyMissionsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    // Inietta le dipendenze con Koin
    private val missionRepository: MissionRepository by inject()
    private val settingsRepository: SettingsRepository by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            // Ottieni l'ID dell'utente corrente
            val userId = settingsRepository.validUserFlow.first()

            if (userId != null) {
                // Esegui la funzione per resettare le missioni settimanali
                missionRepository.setWeeklyMissionsUser(userId)

                // Log per debug
                android.util.Log.d("WeeklyMissionsWorker", "Missioni settimanali aggiornate per utente $userId")

                Result.success()
            } else {
                // Nessun utente loggato, riprova più tardi
                Result.retry()
            }
        } catch (e: Exception) {
            android.util.Log.e("WeeklyMissionsWorker", "Errore nell'aggiornamento missioni", e)
            Result.failure()
        }
    }


}