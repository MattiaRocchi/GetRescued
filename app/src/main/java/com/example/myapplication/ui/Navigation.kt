package com.example.myapplication.ui
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.myapplication.ui.SettingsScreen.SettingsScreen
import com.example.myapplication.ui.SettingsScreen.SettingsViewModel
import com.example.myapplication.ui.addrequest.AddRequestScreen
import com.example.myapplication.ui.addrequest.AddRequestViewModel
import com.example.myapplication.ui.slide.ManageRequestsScreen
import com.example.myapplication.ui.changeProfile.ChangeProfileScreen
import com.example.myapplication.ui.changeProfile.ChangeProfileViewModel
import com.example.myapplication.ui.editrequest.EditRequestScreen
import com.example.myapplication.ui.editrequest.EditRequestViewModel
import com.example.myapplication.ui.inforequest.InfoRequestScreen
import com.example.myapplication.ui.inforequest.InfoRequestViewModel
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.ui.managerequest.ManageRequest
import com.example.myapplication.ui.managerequest.ManageRequestViewModel
import com.example.myapplication.ui.missions.MissionGeneralScreen
import com.example.myapplication.ui.missions.MissionViewModel
import com.example.myapplication.ui.missions.MissionWeekScreen
import com.example.myapplication.ui.profile.ProfileScreen
import com.example.myapplication.ui.profile.ProfileViewModel
import com.example.myapplication.ui.registration.RegistrationScreen
import com.example.myapplication.ui.registration.RegistrationViewModel
import com.example.myapplication.ui.requests.RequestsScreen
import com.example.myapplication.ui.requests.RequestsViewModel
import com.example.myapplication.ui.slide.BrowseRequestsScreen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

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
    data object Login : GetRescuedRoute

    @Serializable
    data object Settings : GetRescuedRoute

    //definisci come data class le route che richiedono parametri quando vendono percorse
    @Serializable
    data class AddRequest(val requestId: Int) : GetRescuedRoute

    @Serializable
    data class ManageRequestDetails(val requestId: Int) : GetRescuedRoute

    @Serializable
    object ManageRequests : GetRescuedRoute

    @Serializable
    object BrowseRequests : GetRescuedRoute

    @Serializable
    object MissionGeneral : GetRescuedRoute

    @Serializable
    object MissionWeek : GetRescuedRoute


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
                // MOLTO SEMPLIFICATO - Koin gestisce tutto
                val viewModel: AddRequestViewModel = koinViewModel()
                AddRequestScreen(viewModel, onCreated = { navController.popBackStack() })
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
            val viewModel: RequestsViewModel = koinViewModel()
            RequestsScreen(navController, viewModel)
        }
        composable<GetRescuedRoute.MissionWeek> {
            val viewModel: MissionViewModel = koinViewModel()
            MissionWeekScreen(navController, viewModel)
        }
        composable<GetRescuedRoute.MissionGeneral> {
            val viewModel: MissionViewModel = koinViewModel()
            MissionGeneralScreen(navController, viewModel)
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
            BrowseRequestsScreen(navController = navController)
        }
        composable<GetRescuedRoute.InfoRequest> { backStackEntry ->
            val args = backStackEntry.toRoute<GetRescuedRoute.InfoRequest>()
            val viewModel: InfoRequestViewModel = koinViewModel { parametersOf(args.requestId) }
            InfoRequestScreen(navController = navController, viewModel = viewModel)
        }
        composable<GetRescuedRoute.ManageRequests> {
            ManageRequestsScreen(navController = navController)
        }
        composable<GetRescuedRoute.EditRequest> { backStackEntry ->
            val args = backStackEntry.toRoute<GetRescuedRoute.EditRequest>()
            val viewModel: EditRequestViewModel = koinViewModel { parametersOf(args.requestId) }
            EditRequestScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable<GetRescuedRoute.ManageRequestDetails> { backStackEntry ->
            val args = backStackEntry.toRoute<GetRescuedRoute.ManageRequestDetails>()
            val viewModel: ManageRequestViewModel = koinViewModel { parametersOf(args.requestId) }
            ManageRequest(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}