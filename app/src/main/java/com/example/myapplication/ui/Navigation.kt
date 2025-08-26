package com.example.myapplication.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.room.Room
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.UserDaoRepository
import com.example.myapplication.dataStore
import com.example.myapplication.ui.SettingsScreen.SettingsScreen
import com.example.myapplication.ui.SettingsScreen.SettingsViewModel

import com.example.myapplication.ui.addrequest.AddRequestScreen
import com.example.myapplication.ui.addrequest.AddRequestViewModel
import com.example.myapplication.ui.slide.ManageRequestsScreen
import com.example.myapplication.ui.changeProfile.ChangeProfileScreen
import com.example.myapplication.ui.changeProfile.ChangeProfileViewModel
import com.example.myapplication.ui.editrequest.EditRequestScreen
import com.example.myapplication.ui.inforequest.InfoRequestScreen
import com.example.myapplication.ui.inforequest.InfoRequestViewModel
import com.example.myapplication.ui.inforequest.InfoRequestViewModelFactory
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.ui.profile.ProfileScreen
import com.example.myapplication.ui.profile.ProfileViewModel
import com.example.myapplication.ui.registration.RegistrationScreen
import com.example.myapplication.ui.registration.RegistrationViewModel
import com.example.myapplication.ui.slide.BrowseRequestsScreen
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


    @Serializable
    data class InfoRequest(val requestId: Int) : GetRescuedRoute

    @Serializable
    data object Requests : GetRescuedRoute

    @Serializable
    data object ChangeProfile : GetRescuedRoute

    @Serializable
    data object Missions : GetRescuedRoute

    @Serializable
    data object Login : GetRescuedRoute

    @Serializable
    data object Settings : GetRescuedRoute

    //definisci come data class le route che richiedono parametri quando vendono percorse
    @Serializable
    data class AddRequest(val requestId: Int) : GetRescuedRoute

    @Serializable
    object ManageRequests : GetRescuedRoute

    @Serializable
    object BrowseRequests : GetRescuedRoute



    @Serializable
    data class EditRequest(val requestId: Int) : GetRescuedRoute



}
@Composable
fun GetRescuedNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: GetRescuedRoute
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
            val context = LocalContext.current
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "rescued-database").build()
            val requestRepository = RequestDaoRepository(db.requestDao())
            val settingsRepository = SettingsRepository(context.dataStore, db.userDao())
            AddRequestScreen(AddRequestViewModel(repository = requestRepository, settingsRepository = settingsRepository), onCreated = {navController.popBackStack()})
        }
        composable<GetRescuedRoute.Profile> {
            val viewModel: ProfileViewModel = koinViewModel()
            ProfileScreen(navController, viewModel)
        }
        composable<GetRescuedRoute.ChangeProfile> {
            val viewModel: ChangeProfileViewModel = koinViewModel()
            ChangeProfileScreen(navController, viewModel)
        }
        composable<GetRescuedRoute.Requests> {
            val context = LocalContext.current
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "rescued-database").build()
            val requestRepository = RequestDaoRepository(db.requestDao())
            RequestsScreen(navController, RequestsViewModel(requestRepository))
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

        composable<GetRescuedRoute.Settings> {
            val viewModel: SettingsViewModel = koinViewModel()
            SettingsScreen(navController, viewModel)
        }

        composable<GetRescuedRoute.BrowseRequests> {
            val context = LocalContext.current
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "rescued-database").build()
            val requestRepository = RequestDaoRepository(db.requestDao())
            val settingsRepository = SettingsRepository(context.dataStore, db.userDao())
            BrowseRequestsScreen(navController = navController, requestRepository = requestRepository, settingsRepository = settingsRepository)
        }


        composable<GetRescuedRoute.InfoRequest> { backStackEntry ->
            val args = backStackEntry.toRoute<GetRescuedRoute.InfoRequest>()
            val context = LocalContext.current
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "rescued-database").build()
            val requestRepository = RequestDaoRepository(db.requestDao())
            val settingsRepository = SettingsRepository(context.dataStore, db.userDao())
            val infoVm: InfoRequestViewModel = viewModel(
                factory = InfoRequestViewModelFactory(
                    requestRepository = requestRepository,
                    userDaoRepository = UserDaoRepository(db.userDao()),
                    settingsRepository = settingsRepository,
                    requestId = args.requestId
                )
            )
            InfoRequestScreen(navController = navController, viewModel = infoVm)
        }

        composable<GetRescuedRoute.ManageRequests> {
            val context = LocalContext.current
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "rescued-database").build()
            val requestRepository = RequestDaoRepository(db.requestDao())
            val settingsRepository = SettingsRepository(context.dataStore, db.userDao())
            ManageRequestsScreen(
                navController = navController,
                requestRepository = requestRepository,
                settingsRepository = settingsRepository
            )
        }
        composable<GetRescuedRoute.EditRequest> { backStackEntry ->
            val args = backStackEntry.toRoute<GetRescuedRoute.EditRequest>()
            val context = LocalContext.current
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "rescued-database").build()
            val requestRepository = RequestDaoRepository(db.requestDao())
            EditRequestScreen(
                navController = navController,
                requestId = args.requestId,
                repository = requestRepository
            )
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


