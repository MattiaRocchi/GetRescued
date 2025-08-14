package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigation.BottomNavBar
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.ui.res.painterResource
import com.example.myapplication.navigation.GetRescuedTopBar
import com.example.myapplication.ui.GetRescuedNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                //TODO non mi piace lo scaffold nella main activity, andrà cambiato
                Scaffold(
                    topBar = {
                        GetRescuedTopBar(
                            navController = navController,
                            profileImage = painterResource(id = R.drawable.ic_profile_placeholder)
                        )
                    },
                    bottomBar = { BottomNavBar(navController) }
                ) { padding ->
                    GetRescuedNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(padding)
                    )
                }
               // GetRescuedNavGraph(navController)
                /*
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }*/
            }
        }
    }
}

/*
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
}*/