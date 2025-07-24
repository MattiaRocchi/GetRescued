package com.example.myapplication.ui.requests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.models.Request

@Composable
fun RequestDetailsScreen(
    //ENTRAMBE DA CREARE
    request: Request,         // Dati della richiesta da visualizzare
    navController: NavController  // Controlla la navigazione tra schermate
) {
    // 1. Scaffold: Struttura base della schermata (con top bar)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(request.title) },  // Titolo della schermata = titolo richiesta
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled., contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        // 2. Contenuto principale (dentro un Column scorrevole)
        Column(
            modifier = Modifier
                .padding(padding)  // Spazio per evitare sovrapposizione con la top bar
                .fillMaxSize()     // Occupa tutto lo spazio disponibile
                .padding(16.dp)    // Margini interni
                .verticalScroll(rememberScrollState())  // Permette lo scrolling
        ) {
            // 3. Sezione Descrizione
            Text("Descrizione:", style = MaterialTheme.typography.subtitle1)
            Text(request.description)  // Mostra il testo della richiesta

            Spacer(modifier = Modifier.height(16.dp))  // Spaziatura

            // 4. Dettagli aggiuntivi
            Text("Difficoltà: ${request.difficulty.displayName}")
            Text("Persone richieste: ${request.peopleNeeded}")

            // 5. Pulsante per partecipare (esempio)
            if (request.status == RequestStatus.OPEN) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { /* Logica per partecipare */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Partecipa alla Richiesta")
                }
            }
        }
    }
}