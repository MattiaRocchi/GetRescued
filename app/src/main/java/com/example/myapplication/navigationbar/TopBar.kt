package com.example.myapplication.navigationbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.ui.GetRescuedRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetRescuedTopBar(
    navController: NavController,
    profileImage: Painter? = null,
    isUserLoggedIn: Boolean = false
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("GetRescued", style = MaterialTheme.typography.titleLarge)
                Image(
                    painter = painterResource(id = R.drawable.ic_red_cross),
                    contentDescription = "Logo Croce Rossa",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        actions = {
            IconButton(onClick = { navController.navigate(GetRescuedRoute.Profile) }) {
                Icon(
                    painter = profileImage ?: painterResource(id = R.drawable.ic_profile_placeholder),
                    contentDescription = "Profilo utente",
                    modifier = Modifier.size(32.dp)
                )
            }
            // Mostra icona settings solo se loggato
            if (isUserLoggedIn) {
                IconButton(onClick = { navController.navigate(GetRescuedRoute.Settings) }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Impostazioni",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface)
    )
}