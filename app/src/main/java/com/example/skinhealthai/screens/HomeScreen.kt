package com.example.skinhealthai.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import com.example.skinhealthai.utils.saveBitmapToFile

@Composable
fun HomeScreen() {
    val context = LocalContext.current

    val gradient = Brush.verticalGradient(
        colors = listOf(Color.Black, Color.DarkGray)
    )

    var showCamera by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.size(40.dp))
                Text(
                    text = "Fábio J.r",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "HOME",
                fontSize = 14.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botões
            HomeButton("Captura de imagens da pele", Color(0xFF2196F3)) {
                showCamera = true
            }

            Spacer(modifier = Modifier.height(24.dp))

            HomeButton("Processamento automático de imagens da pele com IA", Color(0xFF2196F3)) {
                // Futuro
            }

            Spacer(modifier = Modifier.height(24.dp))

            HomeButton("Classificação de risco", Color(0xFF2196F3)) {
                // Futuro
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Exibição da imagem capturada
            capturedBitmap?.let { bitmap ->
                Text(
                    text = "Imagem capturada:",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }

        // Componente de captura de imagem (invocado quando showCamera = true)
        if (showCamera) {
            CameraCapture(
                onImageCaptured = { bitmap ->
                    capturedBitmap = bitmap
                    showCamera = false

                    bitmap?.let {
                        val savedFile = saveBitmapToFile(context, it)
                        if (savedFile != null) {
                            Log.d("SkinHealth", "Imagem salva em: ${savedFile.absolutePath}")
                        } else {
                            Log.e("SkinHealth", "Erro ao salvar imagem.")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun HomeButton(text: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = 16.sp)
    }
}
