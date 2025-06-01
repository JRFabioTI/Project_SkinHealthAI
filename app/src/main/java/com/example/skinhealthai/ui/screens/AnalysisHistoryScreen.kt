package com.example.skinhealthai.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.skinhealthai.data.model.ConsultationResponse
import com.example.skinhealthai.viewmodel.AnalysisHistoryUiState
import com.example.skinhealthai.viewmodel.AnalysisHistoryViewModel
import com.example.skinhealthai.viewmodel.DeleteConsultationUiState
import java.text.SimpleDateFormat
import java.util.Locale
import java.text.ParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisHistoryScreen(
    navController: NavHostController,
    analysisHistoryViewModel: AnalysisHistoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val analysisHistoryState by analysisHistoryViewModel.analysisHistoryState.collectAsState()
    val deleteConsultationState by analysisHistoryViewModel.deleteConsultationState.collectAsState()

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var consultationToDelete by remember { mutableStateOf<ConsultationResponse?>(null) }

    LaunchedEffect(deleteConsultationState) {
        when (deleteConsultationState) {
            is DeleteConsultationUiState.Success -> {
                Toast.makeText(context, "Consulta excluída com sucesso!", Toast.LENGTH_SHORT).show()
                analysisHistoryViewModel.resetDeleteConsultationState()
            }
            is DeleteConsultationUiState.Error -> {
                val errorMessage = (deleteConsultationState as DeleteConsultationUiState.Error).message
                Toast.makeText(context, "Erro ao excluir consulta: $errorMessage", Toast.LENGTH_LONG).show()
                analysisHistoryViewModel.resetDeleteConsultationState()
            }
            else -> { /* Não fazer nada para Idle ou Loading */ }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Consultas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (analysisHistoryState) {
            is AnalysisHistoryUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                    Text("Carregando histórico...", modifier = Modifier.padding(top = 80.dp))
                }
            }
            is AnalysisHistoryUiState.Loaded -> {
                val consultations = (analysisHistoryState as AnalysisHistoryUiState.Loaded).consultations
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (consultations.isEmpty()) {
                        item {
                            Text(
                                text = "Nenhuma consulta encontrada.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    } else {
                        items(consultations) { consultation ->
                            AnalysisHistoryListItem(
                                consultation = consultation,
                                onClick = {
                                    consultation.patientDetails?.id?.let { patientId ->
                                        consultation.id?.let { consultationId ->
                                            navController.navigate(
                                                "${AppRoutes.PATIENT_RECORD_BASE}/${patientId}?consultationId=${consultationId}"
                                            )
                                        }
                                    } ?: run {
                                        Toast.makeText(context, "ID do paciente não encontrado para esta entrada.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onDeleteClick = { clickedConsultation ->
                                    consultationToDelete = clickedConsultation
                                    showDeleteConfirmationDialog = true
                                },
                                onEditClick = { consultationId ->
                                    // TODO: Implementar navegação para tela de edição de consulta
                                    Toast.makeText(context, "Editar consulta ${consultationId}", Toast.LENGTH_SHORT).show()
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
            is AnalysisHistoryUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(
                        text = (analysisHistoryState as AnalysisHistoryUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            is AnalysisHistoryUiState.Idle -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Carregando histórico de consultas...", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }

    if (showDeleteConfirmationDialog && consultationToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmationDialog = false
                consultationToDelete = null
            },
            title = { Text("Confirmar Exclusão da Consulta") },
            text = {
                Text(
                    "Tem certeza que deseja excluir a consulta ${consultationToDelete?.id} " +
                            "do paciente ${consultationToDelete?.patientDetails?.name ?: "Desconhecido"}?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        consultationToDelete?.id?.let {
                            analysisHistoryViewModel.deleteConsultation(it)
                        }
                        showDeleteConfirmationDialog = false
                        consultationToDelete = null
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmationDialog = false
                        consultationToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AnalysisHistoryListItem(
    consultation: ConsultationResponse,
    onClick: () -> Unit,
    onDeleteClick: (ConsultationResponse) -> Unit,
    onEditClick: (Int) -> Unit
) {
    var showActions by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Paciente: ${consultation.patientDetails?.name ?: "Desconhecido"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            val displayDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val apiDateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

            val formattedDate = try {
                apiDateTimeFormat.parse(consultation.dateConsultation)?.let { dateObject ->
                    displayDateFormat.format(dateObject)
                }
            } catch (e: ParseException) {
                null
            } catch (e: Exception) {
                null
            }

            Text(
                text = "Data: ${formattedDate ?: "Não informada"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            consultation.notes?.let { notes ->
                Text(
                    text = "Notas: $notes",
                    style = MaterialTheme.typography.bodySmall
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
                        onDeleteClick(consultation)
                        showActions = false
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir consulta")
                    }

                    IconButton(onClick = {
                        consultation.id?.let { onEditClick(it) }
                        showActions = false
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar consulta")
                    }
                }
            }
        }
    }
}