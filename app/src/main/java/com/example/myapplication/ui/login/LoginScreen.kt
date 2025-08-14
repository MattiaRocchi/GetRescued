package com.example.myapplication.ui.login


import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.myapplication.ui.GetRescuedRoute

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Login Screen")

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            navController.navigate(GetRescuedRoute.Profile)
        }) {
            Text("Vai al profilo")
        }
        TextButton (onClick = {
            navController.navigate(GetRescuedRoute.Registration)
        }) {
            Text("Sei nuovo? Registrati")
        }
    }
}