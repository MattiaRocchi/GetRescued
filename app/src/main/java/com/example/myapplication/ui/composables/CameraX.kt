package com.example.myapplication.ui.composables

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri

@Composable
fun CameraCapture(
    modifier: Modifier = Modifier,
    onImageFile: (Uri) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 🔹 Mantieni lo stato anche dopo la rotazione
    var useFrontCamera by rememberSaveable { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }

    // 🔹 Crea ImageCapture una sola volta
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    // 🔹 PreviewView persistente
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }

    // 🔹 CameraProvider
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // 🔹 Bind/Unbind quando cambia lifecycleOwner o useFrontCamera
    DisposableEffect(lifecycleOwner, useFrontCamera) {
        val cameraProvider = cameraProviderFuture.get()
        val previewUseCase = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA,
                previewUseCase,
                imageCapture
            )
        } catch (exc: Exception) {
            Log.e("CameraCapture", "Camera binding failed", exc)
        }

        onDispose {
            try {
                cameraProviderFuture.get().unbindAll()
            } catch (e: Exception) {
                Log.w("CameraCapture", "Errore in unbind", e)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Preview della fotocamera
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        //Back button
        IconButton(
            onClick = { onBack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Torna indietro",
                tint = Color.White
            )
        }

        // 📸 Pulsante scatto (grande e rotondo)
        IconButton(
            onClick = {
                if (isCapturing) return@IconButton
                isCapturing = true

                val imageFile = createFile(context)
                val outputOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            val savedUri = output.savedUri ?: Uri.fromFile(imageFile)
                            onImageFile(savedUri)
                            isCapturing = false
                        }

                        override fun onError(exc: ImageCaptureException) {
                            Log.e("CameraCapture", "Errore scatto foto: ${exc.message}", exc)
                            Toast.makeText(context, "Errore nello scatto", Toast.LENGTH_SHORT).show()
                            isCapturing = false
                        }
                    }
                )
            },
            enabled = !isCapturing,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .size(60.dp) // dimensione pulsante
                .background(Color.White, CircleShape)
                .border(2.dp, Color.Gray, CircleShape)

        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Scatta Foto",
                tint = Color.Black
            )
        }

        // 🔄 Pulsante switch camera
        IconButton(
            onClick = { useFrontCamera = !useFrontCamera },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Cached, // icona con frecce circolari
                contentDescription = "Cambia fotocamera",
                tint = Color.White
            )
        }
    }

}

// 🔹 Crea file temporaneo
private fun createFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir = context.externalCacheDir
    return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
}
