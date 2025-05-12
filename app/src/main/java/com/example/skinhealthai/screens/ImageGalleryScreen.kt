package com.example.skinhealthai.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.skinhealthai.ui.theme.BluePrimary
import androidx.compose.ui.graphics.Color

@Composable
fun ImageGalleryScreen(navController: NavController, images: List<Bitmap>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Título da tela com tipografia do Material 3
        Text(
            text = "Galeria de Imagens",
            fontSize = 24.sp,
            color = BluePrimary,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge // Ajustado para o Material 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (images.isEmpty()) {
            // Exibindo mensagem quando não houver imagens
            Text(
                text = "Nenhuma imagem disponível",
                fontSize = 18.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            // Lista de imagens com LazyColumn
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(images) { image ->  // Corrigido para usar a lista de imagens diretamente
                    ImageItem(image = image, onClick = {
                        navController.navigate("scan_image/${images.indexOf(image)}") // Corrigido para passar o índice correto
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
            .clickable { onClick() } // Ação de clique na imagem
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp)) // Bordas arredondadas
    ) {
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = "Imagem capturada",
            modifier = Modifier.fillMaxSize() // Preenche toda a área da Box
        )
    }
}
