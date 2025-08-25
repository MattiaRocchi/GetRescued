package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                // ⚡ Ottieni repository (con Koin o Hilt se già usi DI)
                val settingsRepository = get<SettingsRepository>()

                val validUserId by settingsRepository.validUserFlow.collectAsState(initial = null)

                val startDestination = if (validUserId != null) {
                    GetRescuedRoute.Profile
                } else {
                    GetRescuedRoute.Login
                }
                // se l'utente è loggato, avvio la musica
                    if (validUserId != null) {
                        val musicIntent = Intent(this@MainActivity,
                            MusicService::class.java)
                        startService(musicIntent)
                    }

                Scaffold(
                    topBar = {
                        GetRescuedTopBar(
                            navController = navController,
                            profileImage = painterResource(id = R.drawable.ic_profile_placeholder),
                            (validUserId != null)
                        )
                    },
                    bottomBar = { BottomNavBar(navController) }
                ) { padding ->
                    GetRescuedNavGraph(
                        navController = navController,
                        startDestination = startDestination, // 👈 passo io la startDest
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
        
    }
    override fun onDestroy(){
        super.onDestroy()
        val musicIntent = Intent(this, MusicService::class.java)
        stopService(musicIntent)
    }
}