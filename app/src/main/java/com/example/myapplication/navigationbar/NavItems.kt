package com.example.myapplication.navigationbar

import com.example.myapplication.R
import com.example.myapplication.ui.GetRescuedRoute


sealed class NavItem(
    val route: GetRescuedRoute,
    val title: String,
    val iconResId: Int
) {

    object Missions : NavItem(
        route = GetRescuedRoute.MissionWeek,
        title = "Missioni",
        iconResId = R.drawable.ic_missions
    )

    object Requests : NavItem(
        route = GetRescuedRoute.BrowseRequests,
        title = "Richieste",
        iconResId = R.drawable.ic_requests
    )

    object Gestisci : NavItem(
        route = GetRescuedRoute.ManageRequests,
        title = "Gestisci",
        iconResId = R.drawable.ic_gestisci
    )

    companion object {
        val items = listOf(Missions, Requests , Gestisci)
    }
}