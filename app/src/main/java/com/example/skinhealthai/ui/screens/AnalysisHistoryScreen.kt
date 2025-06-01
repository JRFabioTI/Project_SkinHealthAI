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
import androidx.compose.material.icons.filled.Delete // Importar
import androidx.compose.material.icons.filled.Edit // Importar
import androidx.compose.material.icons.filled.MoreVert // Importar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf // Importar
import androidx.compose.runtime.remember // Importar
import androidx.compose.runtime.setValue // Importar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Importar
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

import com.example.skinhealthai.data.model.ConsultationResponse
import com.example.skinhealthai.viewmodel.AnalysisHistoryUiState
import com.example.skinhealthai.viewmodel.AnalysisHistoryViewModel
import com.example.skinhealthai.viewmodel.DeleteConsultationUiState // Importar
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
    val deleteConsultationState by analysisHistoryViewModel.deleteConsultationState.collectAsState() // Observe o estado de exclusão

    // Estados para o diálogo de confirmação de exclusão
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var consultationToDelete by remember { mutableStateOf<ConsultationResponse?>(null) }


    // Observar o estado de exclusão para exibir mensagens de Toast
    LaunchedEffect(deleteConsultationState) {
        when (deleteConsultationState) {
            is DeleteConsultationUiState.Success -> {
                Toast.makeText(context, "Consulta excluída com sucesso!", Toast.LENGTH_SHORT).show()
                analysisHistoryViewModel.resetDeleteConsultationState() // Resetar o estado
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
                                    // CORREÇÃO: Use 'consultation.patientDetails?.id' e a rota correta
                                    // Assumindo que PatientRecordScreen pode receber patientId e talvez consultationId
                                    consultation.patientDetails?.id?.let { patientId ->
                                        // Você pode querer navegar para o prontuário do paciente
                                        navController.navigate("patient_record/${patientId}")
                                        // Ou para uma tela de detalhes da consulta específica
                                        // navController.navigate("consultation_detail/${consultation.id}")
                                    }
                                },
                                onDeleteClick = { clickedConsultation -> // Passa o objeto completo
                                    consultationToDelete = clickedConsultation
                                    showDeleteConfirmationDialog = true
                                },
                                onEditClick = { consultationId ->
                                    // TODO: Implementar navegação para tela de edição de consulta
                                    Toast.makeText(context, "Editar consulta ${consultationId}", Toast.LENGTH_SHORT).show()
                                    // Exemplo: navController.navigate("edit_consultation_screen/${consultationId}")
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

    // Diálogo de Confirmação de Exclusão de Consulta
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

// O resto do AnalysisHistoryListItem está bom.
@Composable
fun AnalysisHistoryListItem(
    consultation: ConsultationResponse, // Recebe ConsultationResponse
    onClick: () -> Unit,
    onDeleteClick: (ConsultationResponse) -> Unit, // Callback para exclusão
    onEditClick: (Int) -> Unit // Callback para edição
) {
    var showActions by remember { mutableStateOf(false) } // Estado para mostrar/esconder ações

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // Alinha conteúdo e botões
    ) {
        Column(modifier = Modifier.weight(1f)) { // Ocupa o máximo de espaço possível
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

        // Ícones de Ação
        Row {
            // Ícone de três pontos para exibir/esconder as ações
            IconButton(onClick = { showActions = !showActions }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
            }

            // Ações de Lixeira e Lápis, visíveis condicionalmente
            AnimatedVisibility(
                visible = showActions,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row {
                    // Ícone de Lixeira para excluir
                    IconButton(onClick = {
                        onDeleteClick(consultation) // Passa o objeto ConsultationResponse completo
                        showActions = false // Esconde os ícones após a ação
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir consulta")
                    }

                    // Ícone de Lápis para editar
                    IconButton(onClick = {
                        consultation.id?.let { onEditClick(it) } // Passa o ID da consulta
                        showActions = false // Esconde os ícones após a ação
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar consulta")
                    }
                }
            }
        }
    }
}