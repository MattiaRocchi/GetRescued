package com.example.myapplication.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.room.Room
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.ui.add.AddRequestScreen
import com.example.myapplication.ui.add.AddRequestViewModel
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.ui.profile.ProfileScreen
import com.example.myapplication.ui.profile.ProfileViewModel
import com.example.myapplication.ui.registration.RegistrationScreen
import com.example.myapplication.ui.registration.RegistrationViewModel
import com.example.myapplication.ui.requests.RequestsScreen
import com.example.myapplication.ui.requests.RequestsViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
sealed interface GetRescuedRoute {
    //definisci come data object le route che non richiedono parametri quando vendono percorse

    @Serializable
    data object Profile : GetRescuedRoute

    @Serializable
    data object Registration : GetRescuedRoute

    //TODO schermata di titolo
    @Serializable
    data object title : GetRescuedRoute



    @Serializable
    data object Requests : GetRescuedRoute

    @Serializable
    data object Missions : GetRescuedRoute

    @Serializable
    data object Login : GetRescuedRoute

    //definisci come data class le route che richiedono parametri quando vendono percorse
    @Serializable
    data class AddRequest(val requestId: Int) : GetRescuedRoute

}
@Composable
fun GetRescuedNavGraph(
    navController: NavHostController,
    startDestination: GetRescuedRoute,
    modifier: Modifier = Modifier
) {


    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<GetRescuedRoute.Registration> {
            val viewModel: RegistrationViewModel = koinViewModel()
            RegistrationScreen(navController, viewModel)
        }
        composable<GetRescuedRoute.AddRequest> { backStackEntry ->
            val args = backStackEntry.toRoute<GetRescuedRoute.AddRequest>()
            val context = LocalContext.current
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "rescued-database").build()
            AddRequestScreen(navController, AddRequestViewModel(db.requestDao()), userId = args.requestId)
        }
        composable<GetRescuedRoute.Profile> {
            val viewModel: ProfileViewModel = koinViewModel()
            ProfileScreen(navController, viewModel)
        }
        composable<GetRescuedRoute.Requests> {
            val context = LocalContext.current
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "rescued-database").build()
            RequestsScreen(navController, RequestsViewModel(db.requestDao()))
        }
        composable<GetRescuedRoute.Missions> {
            Text("Pagina Missioni")
        }
        composable<GetRescuedRoute.Registration> {
            val viewModel: RegistrationViewModel = koinViewModel()
            RegistrationScreen(navController, viewModel)
        }
        composable<GetRescuedRoute.Login> {
            val viewModel: LoginViewModel = koinViewModel()
            LoginScreen(navController, viewModel)
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


