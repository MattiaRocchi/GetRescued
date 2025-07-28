package com.example.myapplication.ui.requests

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetails(navController: NavController, requestId: String) {
    val request = remember {
        FakeRequestDataSource.requests.firstOrNull { it.id == requestId }
            ?: FakeRequest("", "Non trovato", "", "", "")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(request.title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Descrizione:", style = MaterialTheme.typography.titleMedium)
            Text(request.description)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Stato: ${request.status}")
            Text("Difficoltà: ${request.difficulty}")
        }
    }
}