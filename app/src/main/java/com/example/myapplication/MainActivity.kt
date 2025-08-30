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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigationbar.BottomNavBar
import androidx.compose.ui.res.painterResource
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.navigationbar.GetRescuedTopBar
import com.example.myapplication.ui.GetRescuedNavGraph
import com.example.myapplication.ui.GetRescuedRoute
import com.example.myapplication.utils.MusicService
import org.koin.android.ext.android.get
import kotlinx.coroutines.flow.first
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.ui.theme.GetRescuedTheme
import com.example.myapplication.data.database.UserWithInfo
import com.example.myapplication.data.repositories.UserDaoRepository
import com.example.myapplication.utils.WeeklyMissionsScheduler
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var weeklyMissionsScheduler: WeeklyMissionsScheduler
    private var isMusicServiceRunning = false
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var userRepository: UserDaoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        weeklyMissionsScheduler = WeeklyMissionsScheduler(this)
        // Inizializza il repository una sola volta
        settingsRepository = get<SettingsRepository>()
        userRepository = get<UserDaoRepository>()

        setContent {
            GetRescuedTheme {
                val navController = rememberNavController()

                val validUserId by settingsRepository.validUserFlow.collectAsState(initial = null)
                val musicEnabled by settingsRepository.musicEnabledFlow.collectAsState(initial = true)
                var currentUser by remember { mutableStateOf<UserWithInfo?>(null) }

                val startDestination = if (validUserId != null) {
                    GetRescuedRoute.Profile
                } else {
                    GetRescuedRoute.Login
                }

                // Carica i dati dell'utente quando validUserId cambia e refresh periodico
                LaunchedEffect(validUserId) {
                    val userId = validUserId
                    if (userId != null) {
                        // Caricamento iniziale
                        try {
                            val userData = userRepository.getUserWithInfo(userId)
                            currentUser = userData
                        } catch (e: Exception) {
                            currentUser = null
                        }

                        // Refresh periodico ogni 5 secondi quando l'utente è loggato
                        while (true) {
                            kotlinx.coroutines.delay(5000L)
                            try {
                                val refreshedData = userRepository.getUserWithInfo(userId)
                                if (refreshedData?.profileFoto != currentUser?.profileFoto) {
                                    currentUser = refreshedData
                                }
                            } catch (e: Exception) {
                                // Ignora errori durante il refresh
                            }
                        }
                    } else {
                        currentUser = null
                    }
                }

                //Gestione musica e schedulazione reattiva ai cambiamenti di stato
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
                                isUserLoggedIn = true,
                                userProfilePhoto = currentUser?.profileFoto
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

    //Metodi helper per gestire il servizio musica
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