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
