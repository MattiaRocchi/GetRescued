package com.example.myapplication.ui.requests.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.requests.FakeRequest

@Composable
fun RequestItem(request: FakeRequest, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(request.title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(request.description)
            Spacer(Modifier.height(12.dp))

            // Scegli una delle 3 versioni qui sopra
            StatusAndDifficultyRow(request) // Versione 3 (con icone)
        }
    }
}

@Composable
private fun StatusAndDifficultyRow(request: FakeRequest) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Icona stato
        Icon(
            imageVector = when (request.status) {
                "Disponibile" -> Icons.Default.CheckCircle
                else -> Icons.Default.AccessTime
            },
            contentDescription = null,
            tint = when (request.status) {
                "Disponibile" -> Color.Green
                else -> Color.Blue
            },
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            request.status,
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(Modifier.width(16.dp))

        // Icona difficoltà
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = when (request.difficulty) {
                "Media" -> Color(0xFFFFA500) // Arancione
                "Alta" -> Color.Red
                else -> Color.Gray
            },
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            request.difficulty,
            style = MaterialTheme.typography.labelSmall
        )
    }
}