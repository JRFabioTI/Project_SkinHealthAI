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
// REMOVIDO: import com.example.skinhealthai.data.samplePatients
// REMOVIDO: import com.example.skinhealthai.data.model.Patient // Usaremos PatientResponse
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel // Importe viewModel
import com.example.skinhealthai.viewmodel.ImageUploadViewModel
import com.example.skinhealthai.data.model.PatientResponse // Importe PatientResponse
import androidx.compose.runtime.LaunchedEffect // Importe LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.example.skinhealthai.ui.viewmodel.PatientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientListScreen(
    navController: NavHostController,
    patientViewModel: PatientViewModel = viewModel() // Injete PatientViewModel
) {
    var textFieldValue by remember { mutableStateOf("") }
    var showCamera by remember { mutableStateOf(false) }
    var selectedPatientId by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    // Observe a lista de pacientes do ViewModel
    val patientList by patientViewModel.patientList.collectAsState()

    // Acione a busca de pacientes quando a tela é composta pela primeira vez
    LaunchedEffect(Unit) {
        patientViewModel.fetchPatients()
    }

    // FILTRO MELHORADO PARA INCLUIR NOME, CPF E EMAIL
    val filteredPatients = remember(textFieldValue, patientList) {
        if (textFieldValue.isBlank()) {
            patientList
        } else {
            patientList.filter { patient ->
                // Converte o termo de busca para minúsculas uma vez para comparação case-insensitive
                val lowerCaseQuery = textFieldValue.lowercase()

                // Verifica se o nome contém o termo
                val matchesName = patient.name.lowercase().contains(lowerCaseQuery)

                // Verifica se o CPF contém o termo (se o CPF não for nulo)
                val matchesCpf = patient.cpf?.lowercase()?.contains(lowerCaseQuery) ?: false

                // Verifica se o email contém o termo (se o email não for nulo)
                val matchesEmail = patient.email?.lowercase()?.contains(lowerCaseQuery) ?: false

                // Retorna true se corresponder a qualquer um dos campos
                matchesName || matchesCpf || matchesEmail
            }
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
                                navController.navigate("consultation_screen/${patient.id}")
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }

}

@Composable
fun PatientListItem(
    patient: PatientResponse, // Agora espera PatientResponse
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
            // CPF é um bom identificador para exibir na lista
            patient.cpf?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "CPF: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            patient.email?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "Email: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}