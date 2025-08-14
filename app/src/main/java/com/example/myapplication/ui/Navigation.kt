package com.example.myapplication.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.myapplication.R
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.navigation.BottomNavBar
import com.example.myapplication.navigation.GetRescuedTopBar
import com.example.myapplication.navigation.NavItem
import com.example.myapplication.ui.add.AddRequestScreen
import com.example.myapplication.ui.add.AddRequestViewModel
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.ui.profile.ProfileScreen
import com.example.myapplication.ui.registration.RegistrationScreen
import com.example.myapplication.ui.registration.RegistrationViewModel
import com.example.myapplication.ui.requests.RequestsScreen
import com.example.myapplication.ui.requests.RequestsViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel


sealed interface GetRescuedRoute {
    //definisci come data object le route che non richiedono parametri quando vendono percorse

    @Serializable
    data object profile : GetRescuedRoute

    @Serializable
    data object registration : GetRescuedRoute

    //TODO schermata di titolo
    @Serializable
    data object title : GetRescuedRoute

    @Serializable
    data object login : GetRescuedRoute


    //definisci come data class le route che richiedono parametri quando vendono percorse
    @Serializable
    data class addRequest(val requestId: Int) : GetRescuedRoute


}
@Composable
fun GetRescuedNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = GetRescuedRoute.registration,
        modifier = modifier
    ) {
        composable<GetRescuedRoute.registration> {
            val viewModel: RegistrationViewModel = koinViewModel()
            RegistrationScreen(navController, viewModel)
        }
        composable<GetRescuedRoute.login> {
            val viewModel : LoginViewModel = koinViewModel()
            LoginScreen(navController)
        }

        composable<GetRescuedRoute.addRequest> {
            val viewModel: AddRequestViewModel = koinViewModel()
            AddRequestScreen(navController, viewModel, userId = 1)
        }
        composable<GetRescuedRoute.profile> {
            ProfileScreen(navController)
        }

    }
}

/*
@Composable
fun GetRescuedNavGraph(navController: NavHostController


) {


    NavHost(
        navController = navController,
        startDestination = GetRescuedRoute.registration
    ) {
        composable<GetRescuedRoute.addRequest> { backStackEntry ->
            val args = backStackEntry.toRoute<GetRescuedRoute.addRequest>()
            val requestId = args.requestId
            AddRequestScreen(
                navController,
                viewModel = TODO(),
                userId = TODO()
            )
        }
        composable<GetRescuedRoute.profile> {
            ProfileScreen(navController)
        }
        composable<GetRescuedRoute.registration> {
            RegistrationScreen(navController)
        }
    }
}*/


