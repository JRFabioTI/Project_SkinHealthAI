package com.example.skinhealthai.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.graphics.Bitmap
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.layout.ContentScale
import com.example.skinhealthai.viewmodel.ImageUploadViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRecordScreen(
    userName: String,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    imageViewModel: ImageUploadViewModel,
    photoBitmap: Bitmap? = null,
    initialPhotoLocation: String = "ex: antebraço",
    iaAnalysis: String = "Análise IA detalhada sobre a lesão"
) {
    // Estados para campos editáveis
    var photoLocation by remember { mutableStateOf(initialPhotoLocation) }
    var observations by remember { mutableStateOf("") }
    val patientName = "João Silva"
    val patientAge = 45

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Prontuário do Paciente") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
                // Sem actions aqui para evitar confusão
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "$patientName ($patientAge anos)",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            photoBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Foto do paciente",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            Text("Local da Foto:", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = photoLocation,
                onValueChange = { photoLocation = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Informe o local da foto") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Análise IA:", style = MaterialTheme.typography.titleMedium)
            Text(iaAnalysis)

            Spacer(modifier = Modifier.height(12.dp))

            Text("Observações:", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = observations,
                onValueChange = { observations = it },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5,
                placeholder = { Text("Adicione observações...") }
            )
        }
    }
}


