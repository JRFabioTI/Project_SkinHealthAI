package com.example.skinhealthai.screens

import android.graphics.Bitmap
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import com.example.skinhealthai.ui.theme.BluePrimary
import com.example.skinhealthai.ui.theme.BlueSecondary
import com.example.skinhealthai.ui.theme.LightGray
import com.example.skinhealthai.ui.theme.White

@Composable
fun HomeButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BlueSecondary)
            .clickable { onClick() }
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HomeScreen() {
    var showCamera by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cabeçalho
        Text(
            text = "Bem-vindo, Dr. Fábio J.r",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = BluePrimary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Aplicativo de apoio à análise de saúde da pele",
            fontSize = 14.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Botões de ação
        HomeButton("📸 Captura de imagem da pele") {
            showCamera = true
        }

        Spacer(modifier = Modifier.height(16.dp))

        HomeButton("🤖 Processamento automático com IA") {
            // Futuro
        }

        Spacer(modifier = Modifier.height(16.dp))

        HomeButton("⚠️ Classificação de risco") {
            // Futuro
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Imagem capturada
        capturedBitmap?.let { bitmap ->
            Text(
                text = "Imagem capturada:",
                color = BluePrimary,
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

    // Captura da câmera
    if (showCamera) {
        CameraCapture(
            onImageCaptured = { bitmap ->
                capturedBitmap = bitmap
                showCamera = false
            }
        )
    }
}
