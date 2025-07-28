package com.example.myapplication.ui.requests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun RequestsScreen(navController: NavController) {
    // Dati completamente fittizi (hardcoded)
    val fakeRequests = listOf(
        FakeRequest(
            id = "1",
            title = "Riparazione rubinetto",
            description = "Sostituzione guarnizione",
            status = "Disponibile",
            difficulty = "Media"
        ),
        FakeRequest(
            id = "2",
            title = "Aiuto trasloco",
            description = "Carico scatole piano terra",
            status = "In corso",
            difficulty = "Alta"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(fakeRequests) { request ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                onClick = {
                    navController.navigate("request_details/${request.id}")
                }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(request.title, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(request.description)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (request.status) {
                                        "Disponibile" -> Icons.Default.CheckCircle
                                        else -> Icons.Default.AccessTime
                                    },
                                    contentDescription = null,
                                    tint = when (request.status) {
                                        "Disponibile" -> Color.Green
                                        else -> Color.Blue
                                    }
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(request.status)

                                Spacer(Modifier.width(16.dp))

                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Difficoltà",
                                    tint = when (request.difficulty) {
                                        "Media" -> Color.Yellow
                                        "Alta" -> Color.Red
                                        else -> Color.Gray
                                    }
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(request.difficulty)
                    }
                }
            }
        }
    }
}