package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigation.BottomNavBar
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.ui.res.painterResource
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.navigation.GetRescuedTopBar
import com.example.myapplication.ui.GetRescuedNavGraph
import com.example.myapplication.ui.GetRescuedRoute
import com.example.myapplication.utils.MusicService
import org.koin.android.ext.android.get
import kotlinx.coroutines.flow.first
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.utils.WeeklyMissionsScheduler
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var weeklyMissionsScheduler: WeeklyMissionsScheduler
    private var isMusicServiceRunning = false
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        weeklyMissionsScheduler = WeeklyMissionsScheduler(this)
        // Inizializza il repository una sola volta
        settingsRepository = get<SettingsRepository>()

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                val validUserId by settingsRepository.validUserFlow.collectAsState(initial = null)
                val musicEnabled by settingsRepository.musicEnabledFlow.collectAsState(initial = true)

                val startDestination = if (validUserId != null) {
                    GetRescuedRoute.Profile
                } else {
                    GetRescuedRoute.Login
                }

                // 🎵 Gestione musica e schedulazione reattiva ai cambiamenti di stato
                LaunchedEffect(validUserId, musicEnabled) {
                    val isLoggedIn = validUserId != null

                    if (isLoggedIn) {
                        if (musicEnabled) startMusicIfNotRunning() else stopMusicService()
                        weeklyMissionsScheduler.scheduleWeeklyMissionReset()
                    } else {
                        stopMusicService()
                        weeklyMissionsScheduler.cancelWeeklyMissionReset()
                    }
                }


                Scaffold(
                    topBar = {
                        if (validUserId != null) {
                            GetRescuedTopBar(
                                navController = navController,
                                profileImage = painterResource(id = R.drawable.ic_profile_placeholder),
                                true
                            )
                        }
                    },
                    bottomBar = {
                        if (validUserId != null) {
                            BottomNavBar(navController)
                        }
                    }
                ) { padding ->
                    GetRescuedNavGraph(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Ferma la musica quando l'app va in background
        stopMusicService()
    }

    override fun onResume() {
        super.onResume()
        // Riavvia la musica solo se necessario
        lifecycleScope.launch {
            val validUserId = settingsRepository.validUserFlow.first()
            val musicEnabled = settingsRepository.musicEnabledFlow.first()

            if (validUserId != null && musicEnabled) {
                startMusicIfNotRunning()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMusicService()
    }

    // 🎵 Metodi helper per gestire il servizio musica
    private fun startMusicIfNotRunning() {
        if (!isMusicServiceRunning) {
            val musicIntent = Intent(this, MusicService::class.java)
            startService(musicIntent)
            isMusicServiceRunning = true
        }
    }

    private fun stopMusicService() {
        if (isMusicServiceRunning) {
            val musicIntent = Intent(this, MusicService::class.java)
            stopService(musicIntent)
            isMusicServiceRunning = false
        }
    }
}