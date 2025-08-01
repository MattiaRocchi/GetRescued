package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigation.BottomNavBar
import com.example.myapplication.navigation.NavItem
import com.example.myapplication.ui.requests.RequestsScreen
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.Companion.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NavItem.Requests.route,
            modifier = Modifier.Companion.padding(padding)
        ) {
            composable(NavItem.Requests.route) {
                RequestsScreen(navController)
            }
            composable(NavItem.Missions.route) {
                //MissionsScreen(navController)
            }
            composable(NavItem.Profile.route) {
                //ProfileScreen(navController)
            }
            // Aggiungi le rotte per i dettagli
            composable("request_details/{requestId}") { backStackEntry ->
                /*RequestDetailsScreen(
                    navController = navController,
                    requestId = backStackEntry.arguments?.getString("requestId") ?: ""
                )*/
            }
        }
    }
}