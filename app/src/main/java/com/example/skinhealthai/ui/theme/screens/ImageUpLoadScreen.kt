package com.example.skinhealthai.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.skinhealthai.utils.FileUtils
import com.example.skinhealthai.viewmodel.ImageUploadViewModel
import java.io.File

@Composable
fun ImageUploadScreen(viewModel: ImageUploadViewModel = viewModel()) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }
    val prediction by viewModel.predictionResult.collectAsState()

    CameraCapture { capturedBitmap ->
        bitmap = capturedBitmap
        if (capturedBitmap != null) {
            // Salva bitmap em arquivo temporário e enviar para ViewModel
            imageFile = FileUtils.saveBitmapToFile(context, capturedBitmap)
            imageFile?.let { viewModel.uploadImage(it) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Captured Image",
                modifier = Modifier
                    .size(250.dp)
                    .padding(bottom = 16.dp)
            )
        }

        Text(
            text = prediction ?: "Capture uma imagem para processar",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
