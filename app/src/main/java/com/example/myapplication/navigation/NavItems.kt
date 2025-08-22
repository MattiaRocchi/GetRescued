package com.example.myapplication.navigation

import com.example.myapplication.R
import com.example.myapplication.ui.GetRescuedRoute


sealed class NavItem(
    val route: GetRescuedRoute,
    val title: String,
    val iconResId: Int
) {

    object Requests : NavItem(
        route = GetRescuedRoute.Requests,
        title = "Richieste",
        iconResId = R.drawable.ic_requests
    )

    object Missions : NavItem(
        route = GetRescuedRoute.Missions,
        title = "Missioni",
        iconResId = R.drawable.ic_requests
    )

    object Create : NavItem(
        route = GetRescuedRoute.ManageRequests,
        title = "Gestisci",
        iconResId = R.drawable.ic_requests
    )

    companion object {
        val items = listOf(Requests, Missions, Create)
    }
}