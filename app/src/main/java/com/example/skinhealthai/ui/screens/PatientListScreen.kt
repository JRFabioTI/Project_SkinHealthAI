package com.example.skinhealthai.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.skinhealthai.data.samplePatients
import com.example.skinhealthai.data.model.Patient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientListScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Pacientes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    // Botão de voltar para a tela anterior
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        // LazyColumn para exibir a lista de pacientes de forma eficiente
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Aplica o padding do Scaffold
                .padding(horizontal = 16.dp, vertical = 8.dp) // Padding adicional para o conteúdo da lista
        ) {
            // Verifica se a lista de pacientes está vazia
            if (samplePatients.isEmpty()) {
                item {
                    Text(
                        text = "Nenhum paciente cadastrado.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            } else {
                items(samplePatients) { patient ->
                    PatientListItem(
                        patient = patient,
                        onClick = { patientId ->
                            patientId?.let { id ->
                                navController.navigate("patient_record/${id}")
                            } ?: run {
                                println("ID do paciente nulo, não é possível navegar para o registro.")
                            }
                        }
                    )
                    Divider()
                }
            }
        }
    }
}


@Composable
fun PatientListItem(
    patient: Patient,
    onClick: (Int?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(patient.id) }
            .padding(vertical = 12.dp, horizontal = 0.dp)
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