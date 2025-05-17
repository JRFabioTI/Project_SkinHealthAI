package com.example.skinhealthai.ui.screens

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED

@Composable
fun CameraCapture(onImageCaptured: (Bitmap?) -> Unit) {
    val context = LocalContext.current

    var permissionGranted by remember { mutableStateOf(false) }
    var shouldLaunchCamera by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        onImageCaptured(bitmap)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (isGranted) {
            shouldLaunchCamera = true
        }
    }

    LaunchedEffect(Unit) {
        val check = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (check == PERMISSION_GRANTED) {
            permissionGranted = true
            shouldLaunchCamera = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(shouldLaunchCamera) {
        if (shouldLaunchCamera && permissionGranted) {
            cameraLauncher.launch(null)
            shouldLaunchCamera = false
        }
    }
}
