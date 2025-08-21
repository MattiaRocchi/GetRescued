package com.example.myapplication.ui.composables

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import openAddressInMaps

@Composable
fun AddressButton(address: String) {
    val context = LocalContext.current
    Button(onClick = { openAddressInMaps(context, address) }) {
        Text("Apri mappa")
    }
}