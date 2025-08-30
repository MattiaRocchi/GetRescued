package com.example.myapplication.ui.profile

import android.Manifest
import android.content.Intent
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
import com.example.myapplication.data.database.Tags
import com.example.myapplication.ui.GetRescuedRoute
import com.example.myapplication.ui.composables.CameraCapture
import com.example.myapplication.ui.composables.ImagePickerDialog
import com.example.myapplication.ui.theme.LocalTitleColors
import com.example.myapplication.ui.theme.TitleColors
import com.example.myapplication.ui.theme.UnpressableButtonDark
import com.example.myapplication.utils.MusicService
import com.example.myapplication.utils.PermissionStatus
import com.example.myapplication.utils.rememberMultiplePermissions

@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel,
) {
    val context = LocalContext.current


    // Dati dal ViewModel
    val user by viewModel.user.collectAsState()
    val activeTitle by viewModel.userActiveTitle.collectAsState()
    val userTitles by viewModel.userTitles.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val selectedIds = viewModel.getSelectedTagIds()



    // Stati UI
    var showChangePicDialog by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    var showTitleDialog by remember { mutableStateOf(false) }
    var showTagsDialog by remember { mutableStateOf(false) }


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
            LevelProgressBar(user?.exp ?: 0)

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



                Text(
                    text = "📞 ${user?.phoneNumber?: ("Non inserita, " +
                            "cambia i tuoi dati per aggiungere numero di telefono")
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )


            // 🔹 Abitazione (se disponibile)

                Text(
                    text = "🏠 ${
                        user?.habitation ?: ("Non inserita, " +
                                "cambia i tuoi dati per aggiungere abitazione")
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )


            //Cambio titolo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { /* eventualmente aprire dialogo qui */ },
                    modifier = Modifier.weight(1f), // usa peso invece di fillMaxWidth
                    colors = ButtonDefaults.buttonColors(
                        containerColor = rarityToColor(activeTitle?.rarity ?: "Common"),
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = UnpressableButtonDark,
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = activeTitle?.name ?: "Nessun titolo",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(Modifier.width(8.dp))



                IconButton(
                    onClick = { showTitleDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.onSecondaryContainer,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Cambia titolo",
                        tint = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
            }

            Spacer(Modifier.width(18.dp))

            //Gestione Tags
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                Button(
                    onClick = { /* eventualmente aprire dialogo qui */ },
                    modifier = Modifier.weight(1f), // usa peso invece di fillMaxWidth
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledContainerColor = UnpressableButtonDark,
                        disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = "Tags Posseduti: ",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = { showTagsDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.onSecondaryContainer,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Gestisci Tags",
                        tint = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
            }


            Spacer(Modifier.height(18.dp))
            Button(
                onClick = {
                        navController.navigate(GetRescuedRoute.ChangeProfile)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cambia i tuoi dati")
            }

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    viewModel.logout {
                        val musicIntent = Intent(context, MusicService::class.java)
                        context.stopService(musicIntent)

                        navController.navigate(GetRescuedRoute.Login) {
                            // Pulisci tutto il back stack come suggerito dalle slide
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    disabledContainerColor = UnpressableButtonDark,
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary
                ),
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
            titles = userTitles,
            activeTitleId = user?.activeTitle,
            onDismiss = { showTitleDialog = false },
            onSelect = { title ->
                viewModel.updateActiveTitle(title.id)
                showTitleDialog = false
            }
        )
    }

    if (showTagsDialog) {
        TagPickerDialog(
            tags = allTags,
            selectedTagIds = selectedIds,
            onDismiss = { showTagsDialog = false },
            onConfirm = { selectedTags ->
                viewModel.updateUserTags(selectedTags)
                showTagsDialog = false
            }
        )
    }
}
