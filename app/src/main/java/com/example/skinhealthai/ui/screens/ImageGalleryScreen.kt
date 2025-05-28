package com.example.skinhealthai.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.skinhealthai.ui.theme.BluePrimary
import com.example.skinhealthai.utils.ImageStorage

@Composable
fun ImageGalleryScreen(navController: NavHostController) {
    // Lista de imagens
    val images = ImageStorage.getAll()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Galeria de Imagens",
            fontSize = 24.sp,
            color = BluePrimary,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (images.isEmpty()) {
            Text(
                text = "Nenhuma imagem disponível",
                fontSize = 18.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(images) { image ->
                    ImageItem(image = image, onClick = {
                        navController.navigate("scan_image/${images.indexOf(image)}")
                    })
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ImageItem(image: Bitmap, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = "Imagem capturada",
            modifier = Modifier.fillMaxSize()
        )
    }
}
