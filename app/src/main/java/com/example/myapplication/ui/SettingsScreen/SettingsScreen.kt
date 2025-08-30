package com.example.myapplication.ui.SettingsScreen

//TODO Se i permessi risultano permanentemente negati non li richiede più

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.app.Activity
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
    val activity = context as Activity
    val scope = rememberCoroutineScope()
    val musicEnabled by viewModel.musicEnabled.collectAsState(initial = false)
    val musicVolume by viewModel.musicVolume.collectAsState(initial = 0.5f)
    val cameraEnabled by viewModel.cameraEnabled.collectAsState(initial = false)
    val locationEnabled by viewModel.locationEnabled.collectAsState(initial = false)

    val snackbarHostState = remember { SnackbarHostState() }

    // Stati dei permessi
    var cameraPermissionStatus by remember { mutableStateOf(getPermissionStatus(activity, Manifest.permission.CAMERA)) }
    var locationPermissionStatus by remember { mutableStateOf(getPermissionStatus(activity, Manifest.permission.ACCESS_FINE_LOCATION)) }

    // Permission launchers separati
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            cameraPermissionStatus = if (granted) {
                PermissionStatus.Granted
            } else {
                if (activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    PermissionStatus.Denied
                } else {
                    PermissionStatus.PermanentlyDenied
                }
            }

            if (granted) {
                viewModel.setCameraEnabled(true)
            } else {
                viewModel.setCameraEnabled(false)
                if (cameraPermissionStatus == PermissionStatus.Denied) {
                    scope.launch { snackbarHostState.showSnackbar("Permesso fotocamera negato") }
                }
            }
        }
    )

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            locationPermissionStatus = if (granted) {
                PermissionStatus.Granted
            } else {
                if (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    PermissionStatus.Denied
                } else {
                    PermissionStatus.PermanentlyDenied
                }
            }

            if (granted) {
                viewModel.setLocationEnabled(true)
            } else {
                viewModel.setLocationEnabled(false)
                if (locationPermissionStatus == PermissionStatus.Denied) {
                    scope.launch { snackbarHostState.showSnackbar("Permesso posizione negato") }
                }
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
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
            PermissionToggle(
                title = "Usa Fotocamera",
                enabled = cameraEnabled,
                permissionStatus = cameraPermissionStatus,
                onToggle = { enabled ->
                    if (enabled) {
                        when (cameraPermissionStatus) {
                            PermissionStatus.Granted -> viewModel.setCameraEnabled(true)
                            PermissionStatus.Unknown, PermissionStatus.Denied -> {
                                cameraLauncher.launch(Manifest.permission.CAMERA)
                            }
                            PermissionStatus.PermanentlyDenied -> {
                                // Non fare nulla, il toggle è disabilitato
                            }
                        }
                    } else {
                        viewModel.setCameraEnabled(false)
                    }
                }
            )

            // Location permission toggle
            PermissionToggle(
                title = "Usa Posizione (GPS)",
                enabled = locationEnabled,
                permissionStatus = locationPermissionStatus,
                onToggle = { enabled ->
                    if (enabled) {
                        when (locationPermissionStatus) {
                            PermissionStatus.Granted -> viewModel.setLocationEnabled(true)
                            PermissionStatus.Unknown, PermissionStatus.Denied -> {
                                locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                            PermissionStatus.PermanentlyDenied -> {
                                // Non fare nulla, il toggle è disabilitato
                            }
                        }
                    } else {
                        viewModel.setLocationEnabled(false)
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

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

// Enum per gli stati dei permessi (copiato dal tuo file Permission.kt)
enum class PermissionStatus {
    Unknown,
    Granted,
    Denied,
    PermanentlyDenied;

    val isGranted get() = this == Granted
    val isDenied get() = this == Denied || this == PermanentlyDenied
}

// Funzione helper per determinare lo stato del permesso
private fun getPermissionStatus(activity: Activity, permission: String): PermissionStatus {
    return when {
        ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED -> {
            PermissionStatus.Granted
        }
        activity.shouldShowRequestPermissionRationale(permission) -> {
            PermissionStatus.Denied
        }
        else -> {
            PermissionStatus.Unknown
        }
    }
}

@Composable
private fun PermissionToggle(
    title: String,
    enabled: Boolean,
    permissionStatus: PermissionStatus,
    onToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Main toggle row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, modifier = Modifier.weight(1f))
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                enabled = permissionStatus != PermissionStatus.PermanentlyDenied
            )
        }

        // Warning card for permanently denied permission
        if (permissionStatus == PermissionStatus.PermanentlyDenied) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Attenzione",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Permesso negato permanentemente",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Per attivare questa funzione, vai alle impostazioni dell'app",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Impostazioni",
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                        Text(
                            text = "Vai",
                            fontSize = 12.sp
                        )
                    }
                }
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
    <service android:name="com.example.myapplication.utils.MusicService" />

- Aggiungi le dipendenze Gradle (module: app):
    implementation "androidx.datastore:datastore-preferences:1.0.0"
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1"
    implementation "androidx.activity:activity-compose:1.8.0"
    implementation "androidx.compose.material3:material3:1.1.0"

- Come integrare: crea SettingsRepository(context), crea SettingsViewModel(repository) (usa Koin/Hilt o ViewModelProvider), e usa SettingsScreen(viewModel = myViewModel) dentro la NavGraph.

MIGLIORAMENTI (VERSIONE CORRETTA):
- Ogni permesso ha il suo launcher separato per evitare richieste multiple
- Gestione individuale degli stati dei permessi
- Mostra un avviso elegante quando i permessi sono permanentemente negati
- Disabilita lo switch quando il permesso è negato permanentemente
- Fornisce un pulsante diretto per aprire le impostazioni dell'app
- UI più pulita e user-friendly per la gestione degli errori

NOTE:
- Su Android 8+ se vuoi che la musica continui in background in modo affidabile quando l'app è in background per lungo tempo, valuta l'uso di un Foreground Service con Notification.
- Le preferenze camera/location qui memorizzano solo lo "stato desiderato"; la gestione effettiva del permesso (revoca) dipende dal sistema.
- Ogni permesso viene ora gestito singolarmente per evitare richieste indesiderate.
*/