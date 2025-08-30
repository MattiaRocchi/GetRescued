package com.example.myapplication.ui.userrequest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.ui.GetRescuedRoute
import com.example.myapplication.ui.composables.DynamicRequestCard
import org.koin.compose.koinInject

@Composable
fun UserRequestsList(
    navController: NavController,
    viewModel: UserRequestListViewModel
) {
    val request by viewModel.myRequests.collectAsState()
    val filterRequests = request.filter { !it.completed }
    val settingsRepository: SettingsRepository = koinInject()
    val currentUserId by settingsRepository.userIdFlow.collectAsState(initial = -1)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (filterRequests.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Non hai ancora creato richieste.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            items(filterRequests) { request ->
                DynamicRequestCard(
                    request = request,
                    onClick = { navController.navigate(GetRescuedRoute.ManageRequestDetails(request.id)) },
                    currentUserId = currentUserId
                )
            }
        }
    }
}