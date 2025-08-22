package com.example.myapplication.ui.addrequest
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
fun UserRequestsList(
    navController: NavController,
    viewModel: UserRequestListViewModel
) {
    val requests by viewModel.myRequests.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (requests.isEmpty()) {
            item { Text("Non hai ancora creato richieste.", style = MaterialTheme.typography.bodyLarge) }
        } else {
            items(requests) { request ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(GetRescuedRoute.EditRequest(request.id)) },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(request.title, style = MaterialTheme.typography.titleMedium)
                        Text("Difficoltà: ${request.difficulty}", style = MaterialTheme.typography.bodySmall)
                        Text("Persone richieste: ${request.peopleRequired}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}