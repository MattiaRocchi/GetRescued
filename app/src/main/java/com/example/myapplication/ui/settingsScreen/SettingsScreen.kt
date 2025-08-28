package com.example.myapplication.ui.settingsScreen

//TODO Se i permessi risultano permanentemente negati non li richiede più

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.myapplication.utils.MusicService
import kotlinx.coroutines.launch

// -------------------------
// SettingsScreen (Compose)
// - switch Music ON/OFF
// - slider Volume
// - switches for Camera and Location that trigger permission requests
// -------------------------

@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val musicEnabled by viewModel.musicEnabled.collectAsState(initial = false)
    val musicVolume by viewModel.musicVolume.collectAsState(initial = 0.5f)
    val cameraEnabled by viewModel.cameraEnabled.collectAsState(initial = false)
    val locationEnabled by viewModel.locationEnabled.collectAsState(initial = false)

    val snackbarHostState = remember { SnackbarHostState() }

    // Permission launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                viewModel.setCameraEnabled(true)
            } else {
                viewModel.setCameraEnabled(false)
                scope.launch { snackbarHostState.showSnackbar("Permesso fotocamera negato") }
            }
        }
    )

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                viewModel.setLocationEnabled(true)
            } else {
                viewModel.setLocationEnabled(false)
                scope.launch { snackbarHostState.showSnackbar("Permesso posizione negato") }
            }
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {

            // Music ON/OFF
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Musica di Sottofondo", modifier = Modifier.weight(1f))
                Switch(
                    checked = musicEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            // start service
                            viewModel.setMusicEnabled(true)
                            val intent = Intent(context, MusicService::class.java).apply {
                                action = MusicService.ACTION_START
                                putExtra(MusicService.EXTRA_VOLUME, musicVolume)
                            }
                            context.startService(intent)
                        } else {
                            viewModel.setMusicEnabled(false)
                            val intent = Intent(context, MusicService::class.java).apply {
                                action = MusicService.ACTION_STOP
                            }
                            context.stopService(intent)
                        }
                    }
                )
            }

            // Volume slider
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(text = "Volume: ${(musicVolume * 100).toInt()}%")
                Slider(
                    value = musicVolume,
                    onValueChange = { v ->
                        // ottimizzazione locale: aggiornare UI immediatamente
                        viewModel.setMusicVolume(v)
                    },
                    onValueChangeFinished = {
                        // notify service
                        val intent = Intent(context, MusicService::class.java).apply {
                            action = MusicService.ACTION_SET_VOLUME
                            putExtra(MusicService.EXTRA_VOLUME, musicVolume)
                        }
                        context.startService(intent)
                    },
                    valueRange = 0f..1f
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // Camera permission toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Usa Fotocamera", modifier = Modifier.weight(1f))
                Switch(
                    checked = cameraEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            // request permission
                            cameraLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            // simply flip preference (user can revoke in system settings)
                            viewModel.setCameraEnabled(false)
                        }
                    }
                )
            }

            // Location permission toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Usa Posizione (GPS)", modifier = Modifier.weight(1f))
                Switch(
                    checked = locationEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        } else {
                            viewModel.setLocationEnabled(false)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    scope.launch { snackbarHostState.showSnackbar("Impostazioni salvate") }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salva")
            }
        }
    }
}

/*
USO:
- Aggiungi i permessi nel manifest:
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

- Registra il service nel manifest (se non lo fai, il sistema non lo troverà):
    <service android:name="com.example.myapplication.settings.MusicService" />

- Aggiungi le dipendenze Gradle (module: app):
    implementation "androidx.datastore:datastore-preferences:1.0.0"
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1"
    implementation "androidx.activity:activity-compose:1.8.0"
    implementation "androidx.compose.material3:material3:1.1.0"

- Come integrare: crea SettingsRepository(context), crea SettingsViewModel(repository) (usa Koin/Hilt o ViewModelProvider), e usa SettingsScreen(viewModel = myViewModel) dentro la NavGraph.

NOTE:
- Su Android 8+ se vuoi che la musica continui in background in modo affidabile quando l'app è in background per lungo tempo, valuta l'uso di un Foreground Service con Notification.
- Le preferenze camera/location qui memorizzano solo lo "stato desiderato"; la gestione effettiva del permesso (revoca) dipende dal sistema. Se l'utente nega, il toggle tornerà a OFF.
*/
