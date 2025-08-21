package com.example.myapplication.ui.profile

import android.Manifest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.navigation.GetRescuedTopBar
import com.example.myapplication.ui.GetRescuedRoute
import com.example.myapplication.ui.composables.CameraCapture
import com.example.myapplication.utils.PermissionStatus
import com.example.myapplication.utils.rememberMultiplePermissions
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel,
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()

    // Stato UI
    var showChangePicDialog by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }

    // Gestione permesso fotocamera
    val cameraPermissions = rememberMultiplePermissions(
        listOf(Manifest.permission.CAMERA)
    ) { statuses ->
        when {
            statuses[Manifest.permission.CAMERA]?.isGranted == true -> {
                showCamera = true // ✅ apri CameraCapture
            }

            statuses[Manifest.permission.CAMERA] == PermissionStatus.PermanentlyDenied -> {
                Toast.makeText(
                    context,
                    "Permesso fotocamera permanentemente negato. Abilitalo dalle impostazioni.",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                Toast.makeText(
                    context,
                    "Permesso fotocamera negato.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // 🔹 Se la camera è attiva → mostra overlay CameraCapture
    if (showCamera) {
        CameraCapture(
            onImageFile = { uri ->
                // Salva nel DB
                if (user != null) {
                    viewModel.updateProfilePhoto( uri.toString())
                }
                showCamera = false // Chiudi camera dopo scatto
            },
            modifier = Modifier.fillMaxSize()
        )
        return // ⬅️ non disegnare altro sotto
    }

    // 🔹 Contenuto normale del profilo
    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Caricamento profilo...")
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Foto profilo cliccabile
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { showChangePicDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (!user!!.profileFoto.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(user!!.profileFoto),
                        contentDescription = "Foto profilo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = user!!.name.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            LevelProgressBar(90000)

            Spacer(Modifier.height(16.dp))
            Text(
                text = "${user?.name.orEmpty()} ${user?.surname.orEmpty()}",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(4.dp))
            Text(
                text = user?.email.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    viewModel.logout {
                        navController.navigate(GetRescuedRoute.Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }

    // 🔹 Dialog per scelta azione foto
    if (showChangePicDialog) {
        AlertDialog(
            onDismissRequest = { showChangePicDialog = false },
            title = { Text("Cambia foto profilo") },
            text = { Text("Scegli come aggiornare la tua foto profilo") },
            confirmButton = {
                Column {
                    Button(
                        onClick = {
                            showChangePicDialog = false
                            cameraPermissions.launchPermissionRequest()
                        }
                    ) { Text("Scatta foto") }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            showChangePicDialog = false
                            // TODO: Apri gallery picker
                        }
                    ) { Text("Scegli dalla galleria") }
                }
            },
            dismissButton = {
                Button(onClick = { showChangePicDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }
}