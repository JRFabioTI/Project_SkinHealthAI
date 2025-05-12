package com.example.skinhealthai.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.skinhealthai.ui.theme.BluePrimary

@Composable
fun ScanImageScreen(navController: NavController, image: Bitmap) {
    var scanResult by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título da tela com o estilo do MaterialTheme
        Text(
            text = "Escaneando Imagem",
            fontSize = 24.sp,
            color = BluePrimary,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall // Usando headlineSmall do MaterialTheme para manter o padrão
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Exibição da imagem escaneada
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = "Imagem escaneada",
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botão para iniciar o escaneamento
        Button(
            onClick = {
                // Aqui você integrará a IA para processar a imagem
                // Vamos simular um resultado de escaneamento para demonstrar
                scanResult = "Resultado: Sem evidências de câncer de pele."
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar Escaneamento", fontSize = 16.sp, color = BluePrimary)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Exibindo o resultado do escaneamento, se houver
        scanResult?.let {
            Text(
                text = it,
                fontSize = 18.sp,
                color = BluePrimary,
                style = MaterialTheme.typography.bodyLarge // Usando bodyLarge do MaterialTheme para manter o padrão
            )
        }
    }
}
