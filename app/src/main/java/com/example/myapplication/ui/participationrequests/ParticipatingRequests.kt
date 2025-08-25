package com.example.myapplication.ui.participationrequests


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.ui.GetRescuedRoute

@Composable
fun ParticipatingRequests(
    navController: NavController,
    viewModel: ParticipatingRequestsViewModel
) {
    val requests by viewModel.participation.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (requests.isEmpty()) {
            item { Text("Non stai partecipando a nessuna richiesta.", style = MaterialTheme.typography.bodyLarge) }
        } else {
            items(requests) { req ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(GetRescuedRoute.InfoRequest(req.id)) },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(req.title, style = MaterialTheme.typography.titleMedium)
                        Text("Partecipanti: ${req.rescuers.size}/${req.peopleRequired}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}