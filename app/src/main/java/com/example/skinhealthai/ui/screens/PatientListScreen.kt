package com.example.skinhealthai.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.skinhealthai.data.samplePatients
import com.example.skinhealthai.data.model.Patient
import androidx.compose.ui.platform.LocalContext
import com.example.skinhealthai.utils.FileUtils
import com.example.skinhealthai.viewmodel.ImageUploadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientListScreen(
    navController: NavHostController,
    imageViewModel: ImageUploadViewModel // ViewModel para armazenar imagem capturada
) {
    var textFieldValue by remember { mutableStateOf("") }
    var showCamera by remember { mutableStateOf(false) }
    var selectedPatientId by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    // Filtra pacientes conforme o texto no filtro
    val filteredPatients = remember(textFieldValue) {
        if (textFieldValue.isBlank()) samplePatients
        else samplePatients.filter {
            it.name.contains(textFieldValue, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escolha um paciente da sua lista", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                label = { Text("Filtro") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true
            )

            LazyColumn {
                if (filteredPatients.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhum paciente encontrado.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                } else {
                    items(filteredPatients) { patient ->
                        PatientListItem(
                            patient = patient,
                            onClick = {
                                selectedPatientId = patient.id
                                showCamera = true
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }

    // Se showCamera for true, abre o componente de captura
    if (showCamera && selectedPatientId != null) {
        CameraCapture(
            onImageCaptured = { bitmap ->
                showCamera = false
                bitmap?.let {
                    FileUtils.saveBitmapToGallery(context, it)
                    imageViewModel.capturedBitmap = it
                }
                // Navega para prontuário do paciente selecionado
                navController.navigate("patient_record/${selectedPatientId}")
                selectedPatientId = null
            },
        )
    }
}

@Composable
fun PatientListItem(
    patient: Patient,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Column {
            Text(
                text = patient.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            patient.email.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
