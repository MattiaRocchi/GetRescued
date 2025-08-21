package com.example.myapplication.ui.composables

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CameraCapture(
    modifier: Modifier = Modifier,
    onImageFile: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    var useFrontCamera by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }

    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }

    // 🔹 PreviewView di CameraX
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }

    // 🔹 Ribinda la camera quando cambia useFrontCamera
    LaunchedEffect(useFrontCamera) {
        val cameraProvider = cameraProviderFuture.get()
        val previewUseCase = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
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
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // 🔹 Pulsante scatto
        Button(
            onClick = {
                if (isCapturing) return@Button
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
                .padding(16.dp)
        ) {
            Text(if (isCapturing) "Scatto..." else "Scatta Foto")
        }

        // 🔹 Pulsante switch camera
        Button(
            onClick = { useFrontCamera = !useFrontCamera },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Text("Switch")
        }
    }
}

// 🔹 Crea un file temporaneo
private fun createFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir = context.externalCacheDir
    return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
}
