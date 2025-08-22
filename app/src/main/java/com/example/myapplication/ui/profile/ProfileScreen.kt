package com.example.myapplication.ui.profile

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.ui.GetRescuedRoute
import com.example.myapplication.ui.composables.CameraCapture
import com.example.myapplication.ui.composables.ImagePickerDialog
import com.example.myapplication.utils.PermissionStatus
import com.example.myapplication.utils.rememberMultiplePermissions

@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel,
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()

    // Stati UI
    var showChangePicDialog by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    var showTitleDialog by remember { mutableStateOf(false) }
    val titles by viewModel.userTitles.collectAsState()
    //togli dopo
    val allTitles by viewModel.userTitles.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.getUserTitles()
    }
    // 📂 Launcher per la galleria
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (user != null) {
                viewModel.updateProfilePhoto(it.toString()) // ✅ salva nel DB
            }
        }
    }


    // 📸 Gestione permessi fotocamera
    val cameraPermissions = rememberMultiplePermissions(
        listOf(Manifest.permission.CAMERA)
    ) { statuses ->
        when {
            statuses[Manifest.permission.CAMERA]?.isGranted == true -> {
                showCamera = true
            }
            statuses[Manifest.permission.CAMERA] == PermissionStatus.PermanentlyDenied -> {
                Toast.makeText(
                    context,
                    "Permesso fotocamera permanentemente negato. Abilitalo dalle impostazioni.",
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                Toast.makeText(context, "Permesso fotocamera negato.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🔹 Se la camera è attiva → mostra overlay CameraCapture
    if (showCamera) {
        CameraCapture(
            onImageFile = { uri ->
                if (user != null) {
                    viewModel.updateProfilePhoto(uri.toString()) // ✅ salva nel DB
                }
                showCamera = false
            },
            onBack = { showCamera = false }, // 🔙 torna indietro senza scattare
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    // 🔹 Schermata profilo normale
    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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


            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titles.firstOrNull { it.id == user?.activeTitle }?.name ?: "Nessun titolo",
                    style = MaterialTheme.typography.bodyLarge
                )

                IconButton(
                    onClick = { showTitleDialog = true },
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.onSecondaryContainer, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Cambia titolo",
                        tint = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
            }

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

    // 🔹 Dialog scelta immagine (camera o galleria)
    if (showChangePicDialog) {
        ImagePickerDialog(
            onDismiss = { showChangePicDialog = false },
            onTakePhoto = {
                showChangePicDialog = false
                cameraPermissions.launchPermissionRequest()
            },
            onPickFromGallery = {
                showChangePicDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }



    if (showTitleDialog) {
        TitlePickerDialog(
            titles = allTitles,
            activeTitleId = user?.activeTitle,
            onDismiss = { showTitleDialog = false },
            onSelect = { title ->
                viewModel.updateActiveTitle(title.id)
                showTitleDialog = false
            }
        )
    }
}
