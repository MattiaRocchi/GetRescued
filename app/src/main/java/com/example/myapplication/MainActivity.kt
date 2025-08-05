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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.room.Room
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.navigation.GetRescuedTopBar
import com.example.myapplication.ui.add.AddRequestScreen
import com.example.myapplication.ui.add.AddRequestViewModel
import com.example.myapplication.ui.requests.RequestsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            GetRescuedTopBar(
                navController = navController,
                profileImage = painterResource(id = R.drawable.ic_profile_placeholder)
            )
        },
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NavItem.Requests.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(NavItem.Requests.route) {
                val context = LocalContext.current
                val db = Room.databaseBuilder(context, AppDatabase::class.java, "rescued-database").build()
                val viewModel = RequestsViewModel(db.requestDao())
                RequestsScreen(navController, viewModel)
            }
            composable(NavItem.Missions.route) {
                // MissionsScreen(navController)
            }
            composable(NavItem.Create.route) {
                val context = LocalContext.current
                val db = Room.databaseBuilder(context, AppDatabase::class.java, "rescued-database").build()
                val viewModel = AddRequestViewModel(db.requestDao())
                AddRequestScreen(navController, viewModel, userId = 1) // userId da gestire correttamente
            }
            composable("request_details/{requestId}") { backStackEntry ->
                /* RequestDetailsScreen(
                    navController = navController,
                    requestId = backStackEntry.arguments?.getString("requestId") ?: ""
                ) */
            }
        }
    }
}