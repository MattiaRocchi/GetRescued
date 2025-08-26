package com.example.myapplication.ui.participationrequests

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
fun ParticipatingRequests(
    navController: NavController,
    viewModel: ParticipatingRequestsViewModel
) {
    val requests by viewModel.participation.collectAsState()
    val settingsRepository: SettingsRepository = koinInject()
    val currentUserId by settingsRepository.userIdFlow.collectAsState(initial = -1)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (requests.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Non stai partecipando a nessuna richiesta.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            items(requests) { request ->
                DynamicRequestCard(
                    request = request,
                    onClick = { navController.navigate(GetRescuedRoute.InfoRequest(request.id)) },
                    currentUserId = currentUserId
                )
            }
        }
    }
}