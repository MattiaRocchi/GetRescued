package com.example.myapplication.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit


// 2. Manager per schedulare il worker
class WeeklyMissionsScheduler(private val context: Context) {

    fun scheduleWeeklyMissionReset() {
        // Calcola il delay fino al prossimo lunedì a mezzanotte
        val nextMondayMidnight = getNextMondayMidnight()
        val currentTime = System.currentTimeMillis()
        val delayInMillis = nextMondayMidnight - currentTime

        // Crea la richiesta di lavoro
        val weeklyMissionWork = PeriodicWorkRequestBuilder<WeeklyMissionsWorker>(
            7, TimeUnit.DAYS // Ripeti ogni 7 giorni
        )
            .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .addTag("weekly_missions_reset")
            .build()

        // Schedula il worker
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "weekly_missions_reset",
                ExistingPeriodicWorkPolicy.REPLACE,
                weeklyMissionWork
            )

        android.util.Log.d("WeeklyMissionsScheduler",
            "Schedulato reset missioni per ${Date(nextMondayMidnight)}")
    }

    private fun getNextMondayMidnight(): Long {
        val calendar = Calendar.getInstance().apply {
            // Vai al prossimo lunedì
            val daysUntilMonday = (Calendar.MONDAY - get(Calendar.DAY_OF_WEEK) + 7) % 7
            if (daysUntilMonday == 0) {
                // Se oggi è lunedì, vai al prossimo lunedì
                add(Calendar.DAY_OF_YEAR, 7)
            } else {
                add(Calendar.DAY_OF_YEAR, daysUntilMonday)
            }

            // Imposta a mezzanotte
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return calendar.timeInMillis
    }

    // Metodo per cancellare la schedulazione (utile per logout)
    fun cancelWeeklyMissionReset() {
        WorkManager.getInstance(context)
            .cancelAllWorkByTag("weekly_missions_reset")
    }
}