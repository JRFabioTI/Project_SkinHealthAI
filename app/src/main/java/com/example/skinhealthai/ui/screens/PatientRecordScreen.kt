package com.example.skinhealthai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.skinhealthai.data.samplePatients
import com.example.skinhealthai.viewmodel.ImageUploadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRecordScreen(
    navController: NavHostController,
    patientId: Int?,
    imageViewModel: ImageUploadViewModel
) {

    val patient = remember(patientId) {
        samplePatients.find { it.id == patientId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Prontuário",
                        fontWeight = FontWeight.Bold
                    )
                },
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
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (patient == null) {
                Text(
                    text = if (patientId == null) "Nenhum paciente selecionado." else "Paciente com ID $patientId não encontrado.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    text = "Nome Completo: ${patient.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Email: ${patient.email}",
                    style = MaterialTheme.typography.bodyLarge
                )
                patient.birthDate?.let {
                    Text(
                        text = "Data de Nascimento: $it",
                        style = MaterialTheme.typography.bodyLarge
                    )

                } ?: run {
                    Text(
                        text = "Data de Nascimento: Não informada",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }


            }
        }
    }
}