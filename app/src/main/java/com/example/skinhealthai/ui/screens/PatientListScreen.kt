package com.example.skinhealthai.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.skinhealthai.data.model.PatientResponse
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.example.skinhealthai.ui.viewmodel.DeletePatientUiState
import com.example.skinhealthai.ui.viewmodel.PatientViewModel
import com.example.skinhealthai.ui.viewmodel.PatientListUiState
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientListScreen(
    navController: NavHostController,
    patientViewModel: PatientViewModel = viewModel()
) {
    var textFieldValue by remember { mutableStateOf("") }
    val context = LocalContext.current

    val patientListState by patientViewModel.patientList.collectAsState()
    val deleteState by patientViewModel.deleteState.collectAsState()

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var patientToDelete by remember { mutableStateOf<PatientResponse?>(null) }


    LaunchedEffect(Unit) {
        patientViewModel.fetchPatients()
    }

    LaunchedEffect(deleteState) {
        when (deleteState) {
            is DeletePatientUiState.Success -> {
                Toast.makeText(context, "Paciente excluído com sucesso!", Toast.LENGTH_SHORT).show()
                patientViewModel.resetDeleteState()
            }
            is DeletePatientUiState.Error -> {
                val errorMessage = (deleteState as DeletePatientUiState.Error).message
                Toast.makeText(context, "Erro ao excluir: $errorMessage", Toast.LENGTH_LONG).show()
                patientViewModel.resetDeleteState()
            }
            else -> { /* Não fazer nada para Idle ou Loading */ }
        }
    }

    val currentPatients: List<PatientResponse> = when (patientListState) {
        is PatientListUiState.Loaded -> (patientListState as PatientListUiState.Loaded).patients
        else -> emptyList()
    }

    val filteredPatients = remember(textFieldValue, currentPatients) {
        if (textFieldValue.isBlank()) {
            currentPatients
        } else {
            currentPatients.filter { patient ->
                val lowerCaseQuery = textFieldValue.lowercase()
                val matchesName = patient.name.lowercase().contains(lowerCaseQuery)
                val matchesCpf = patient.cpf?.lowercase()?.contains(lowerCaseQuery) ?: false
                val matchesEmail = patient.email?.lowercase()?.contains(lowerCaseQuery) ?: false
                matchesName || matchesCpf || matchesEmail
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Pacientes", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Selecione um paciente da sua lista para criar uma nova consulta.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                label = { Text("Filtro") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true
            )

            when (patientListState) {
                is PatientListUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                        Text("Carregando pacientes...", modifier = Modifier.padding(top = 80.dp))
                    }
                }
                is PatientListUiState.Loaded -> {
                    if (filteredPatients.isEmpty()) {
                        Text(
                            text = "Nenhum paciente encontrado. Cadastre um paciente",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    } else {
                        LazyColumn {
                            items(filteredPatients) { patient ->
                                PatientListItem(
                                    patient = patient,
                                    onClick = {
                                        navController.navigate("${AppRoutes.CONSULTATION_SCREEN_BASE}/${patient.id}")
                                    },
                                    onDeleteClick = { clickedPatient ->
                                        patientToDelete = clickedPatient
                                        showDeleteConfirmationDialog = true
                                    },
                                    onEditClick = { patientId ->
                                        navController.navigate(AppRoutes.PATIENT_REGISTER_WITH_ID.replace("{patientId}", patientId.toString()))
                                    }
                                )
                                Divider()
                            }
                        }
                    }
                }
                is PatientListUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = (patientListState as PatientListUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                is PatientListUiState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aguardando carregamento de pacientes...", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }

    if (showDeleteConfirmationDialog && patientToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmationDialog = false
                patientToDelete = null
            },
            title = { Text("Confirmar Exclusão") },
            text = {
                Text(
                    "Tem certeza que deseja excluir o paciente ${patientToDelete?.name}?\n\n" +
                            "AVISO: Todas as consultas relacionadas a este paciente também serão excluídas."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        patientToDelete?.id?.let {
                            patientViewModel.deletePatient(it)
                        }
                        showDeleteConfirmationDialog = false
                        patientToDelete = null
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmationDialog = false
                        patientToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PatientListItem(
    patient: PatientResponse,
    onClick: () -> Unit,
    onDeleteClick: (PatientResponse) -> Unit,
    onEditClick: (Int) -> Unit
) {
    var showActions by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = patient.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Light
            )
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

        Row {
            IconButton(onClick = { showActions = !showActions }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
            }

            AnimatedVisibility(
                visible = showActions,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row {
                    IconButton(onClick = {
                        onDeleteClick(patient)
                        showActions = false
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir paciente")
                    }

                    IconButton(onClick = {
                        onEditClick(patient.id)
                        showActions = false
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar paciente")
                    }
                }
            }
        }
    }
}