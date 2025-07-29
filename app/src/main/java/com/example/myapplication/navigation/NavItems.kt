package com.example.myapplication.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.example.myapplication.R

sealed class NavItem(
    val route: String,
    val title: String,
    val iconResId: Int
) {
    object Requests : NavItem(
        route = "requests",
        title = "Richieste",
        iconResId = R.drawable.ic_requests
    )

    object Missions : NavItem(
        route = "missions",
        title = "Missioni",
        iconResId = R.drawable.ic_requests
    )

    object Profile : NavItem(
        route = "profile",
        title = "Profilo",
        iconResId = R.drawable.ic_requests
    )

    companion object {
        val items = listOf(Requests, Missions, Profile)
    }
}