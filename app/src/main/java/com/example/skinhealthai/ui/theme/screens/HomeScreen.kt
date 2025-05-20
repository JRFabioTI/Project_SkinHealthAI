package com.example.skinhealthai.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.skinhealthai.ui.theme.*
import com.example.skinhealthai.utils.FileUtils
import com.example.skinhealthai.viewmodel.ImageUploadViewModel

@Composable
fun HomeScreen(
    navController: NavHostController,
    imageViewModel: ImageUploadViewModel
) {
    var showCamera by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current
    val uiState by imageViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        HomeButton(text = "📸 Captura de imagem da pele") {
            showCamera = true
        }

        Spacer(modifier = Modifier.height(16.dp))

        HomeButton(text = "🤖 Processamento com IA") {
            navController.navigate("image_gallery")
        }

        Spacer(modifier = Modifier.height(16.dp))

        HomeButton(text = "⚠️ Classificação de risco") {
            // Em construção
        }

        Spacer(modifier = Modifier.height(32.dp))

        capturedBitmap?.let { bitmap ->
            Text(
                text = "Imagem capturada:",
                color = BluePrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
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

        when {
            uiState.isLoading -> {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
                Text(
                    text = "Analisando imagem...",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            uiState.result != null -> {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "✅ Resultado da IA:",
                    color = BluePrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.result ?: "",
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            uiState.error != null -> {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "❌ Erro ao analisar imagem:",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.error ?: "",
                    fontSize = 14.sp,
                    color = Color.Red
                )
            }
        }
    }

    if (showCamera) {
        CameraCapture(
            onImageCaptured = { bitmap ->
                capturedBitmap = bitmap
                showCamera = false

                bitmap?.let {
                    // Salva na galeria (com fallback para cache)
                    val imageFile = FileUtils.saveBitmapToGallery(context, it)
                    if (imageFile != null) {
                        imageViewModel.uploadImage(imageFile)
                    } else {
                        // fallback se não conseguir salvar
                        val fallbackFile = FileUtils.saveBitmapToFile(context, it)
                        imageViewModel.uploadImage(fallbackFile)
                    }
                }
            }
        )
    }
}

@Composable
fun HomeButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = BlueSecondary)
    ) {
        Text(
            text = text,
            color = White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
