package com.example.skinhealthai.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skinhealthai.R



@Composable
fun HomeScreen() {

    val gradient = Brush.verticalGradient(
        colors = listOf(Color.Black, Color.DarkGray)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Top Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iconcamera),
                    contentDescription = "Icone",
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "Fábio J.r",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    painter = painterResource(id = R.drawable.iconperfil),
                    contentDescription = "Perfil",
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "HOME",
                fontSize = 14.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botões
            HomeButton("Captura de imagens da pele", Color(0xFF2196F3))
            Spacer(modifier = Modifier.height(24.dp))
            HomeButton("Processamento automático de imagens da pele com IA", Color(0xFF2196F3))
            Spacer(modifier = Modifier.height(24.dp))
            HomeButton("Classificação de risco", Color(0xFF2196F3))
        }
    }
}

@Composable
fun HomeButton(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .clickable { /* ação futura */ }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = 16.sp)
    }
}
