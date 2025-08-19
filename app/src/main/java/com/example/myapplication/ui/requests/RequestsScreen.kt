package com.example.myapplication.ui.requests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.database.Request

@Composable
fun RequestsScreen(
    navController: NavController,
    viewModel: RequestsViewModel
) {
    val requests by viewModel.requests.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Richieste disponibili", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        if (requests.isEmpty()) {
            Text("Nessuna richiesta trovata.")
        } else {
            LazyColumn {
                items(requests) { request ->
                    RequestItem(request = request)
                    Divider()
                }
            }
        }
    }
}

@Composable
fun RequestItem(request: Request) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Text("Titolo: ${request.title}", style = MaterialTheme.typography.titleMedium)
        Text("Difficoltà: ${request.difficulty}")
        Text("Descrizione: ${request.description}")
        Text("Persone richieste: ${request.peopleRequired}")
    }
}