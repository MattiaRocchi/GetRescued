package com.example.myapplication.ui.composables

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CameraCapture(
    modifier: Modifier = Modifier,
    onImageFile: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var preview by remember { mutableStateOf<Preview?>(null) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }

    // 🔹 PreviewView di CameraX
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val previewUseCase = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        previewUseCase,
                        imageCapture
                    )
                } catch (exc: Exception) {
                    Log.e("CameraCapture", "Camera binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(context))

            previewView
        },
        modifier = modifier.fillMaxSize()
    )

    // 🔹 Pulsante di scatto
    Button(
        onClick = {
            val imageFile = createFile(context)
            val outputOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

            imageCapture?.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = output.savedUri ?: Uri.fromFile(imageFile)
                        Toast.makeText(context, "Foto salvata: $savedUri", Toast.LENGTH_SHORT).show()
                        onImageFile(savedUri)
                    }

                    override fun onError(exc: ImageCaptureException) {
                        Log.e("CameraCapture", "Errore scatto foto: ${exc.message}", exc)
                    }
                }
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Scatta Foto", style = MaterialTheme.typography.bodyLarge)
    }
}

// 🔹 Crea un file temporaneo per la foto
private fun createFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir = context.externalCacheDir
    return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
}