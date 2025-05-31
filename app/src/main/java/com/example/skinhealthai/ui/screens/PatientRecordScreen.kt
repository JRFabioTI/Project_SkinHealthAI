package com.example.skinhealthai.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter // Para carregar a imagem
import com.example.skinhealthai.ui.viewmodel.ConsultationViewModel // Seu ViewModel de Consulta
import com.example.skinhealthai.ui.viewmodel.PatientDataUiState // Estados de UI do paciente
import com.example.skinhealthai.ui.viewmodel.PatientConsultationsUiState // Importe o novo estado
import com.example.skinhealthai.data.model.ConsultationResponse // Importe para usar na lista de consultas
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.text.ParseException // Importe para lidar com erros de parsing de data

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRecordScreen(
    navController: NavController,
    patientId: Int?,
    consultationViewModel: ConsultationViewModel = viewModel() // Injete o ViewModel
) {

    // Observe os estados do ViewModel para os dados do paciente e histórico de consultas
    val patientUiState by consultationViewModel.patientDataUiState.collectAsState()
    val patientConsultationsUiState by consultationViewModel.patientConsultationsUiState.collectAsState()

    // Acionar o carregamento dos dados do paciente e suas consultas quando a tela é iniciada
    LaunchedEffect(patientId) {
        if (patientId != null) {
            consultationViewModel.loadPatient(patientId)
            consultationViewModel.loadPatientConsultations(patientId) // Ainda carrega todas para pegar a mais recente
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Prontuário do Paciente",
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Adicionado para permitir scroll em conteúdo longo
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Exibir informações do paciente com base no estado do ViewModel
            when (patientUiState) {
                is PatientDataUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    Text("Carregando dados do paciente...", modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is PatientDataUiState.PatientLoaded -> {
                    val patient = (patientUiState as PatientDataUiState.PatientLoaded).patient

                    Text(
                        text = "Nome Completo: ${patient.name}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    patient.email?.let {
                        Text(
                            text = "Email: $it",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    patient.cpf?.let {
                        Text(
                            text = "CPF: $it",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    // Formata e exibe a data de nascimento do paciente
                    patient.date_of_birth?.let { apiDateString ->
                        val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Formato que a API retorna

                        val formattedDate = try {
                            apiFormat.parse(apiDateString)?.let { dateObject ->
                                displayFormat.format(dateObject)
                            }
                        } catch (e: ParseException) {
                            null // Retorna null se não conseguir parsear
                        } catch (e: Exception) {
                            null
                        }

                        if (formattedDate != null) {
                            Text(
                                text = "Data de Nascimento: $formattedDate",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            Text(
                                text = "Data de Nascimento: Não informada",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } ?: run {
                        Text(
                            text = "Data de Nascimento: Não informada",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    patient.cellphone?.let { // Assumindo que o campo é 'phone' no PatientResponse
                        Text(
                            text = "Telefone: $it",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Dados da Última Consulta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    // Exibir os dados da última consulta (a mais recente, por exemplo)
                    when (patientConsultationsUiState) {
                        is PatientConsultationsUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            Text("Carregando dados da consulta...", modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                        is PatientConsultationsUiState.Loaded -> {
                            val consultations = (patientConsultationsUiState as PatientConsultationsUiState.Loaded).consultations
                            // Encontrar a consulta mais recente (assumindo que a API retorna em ordem ou você ordena aqui)
                            val latestConsultation = consultations.maxByOrNull {
                                try {
                                    // Parse a data da API para um objeto Date para comparação
                                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(it.dateConsultation) ?: Date(0)
                                } catch (e: Exception) {
                                    Date(0) // Retorna Date(0) em caso de exceção para que seja tratada como muito antiga
                                }
                            }

                            if (latestConsultation != null) {
                                // Formata a data da consulta para exibição
                                val displayDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                val apiDateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

                                val formattedConsultationDate = try {
                                    apiDateTimeFormat.parse(latestConsultation.dateConsultation)?.let { dateObject ->
                                        displayDateFormat.format(dateObject)
                                    }
                                } catch (e: ParseException) {
                                    null
                                } catch (e: Exception) {
                                    null
                                }

                                Text(
                                    text = "Data da Consulta: ${formattedConsultationDate ?: "Não informada"}", // Mostra "Não informada" se o formato der errado
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                latestConsultation.photoLocation?.let { photoLoc ->
                                    Text(
                                        text = "Local da Foto: ${photoLoc}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    // Exibir a imagem se photoLoc for uma URL válida
                                    if (photoLoc.startsWith("http://") || photoLoc.startsWith("https://")) {
                                        Image(
                                            painter = rememberImagePainter(data = photoLoc),
                                            contentDescription = "Foto da Consulta",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        // Se for uma descrição de texto ou um caminho local que não pode ser carregado diretamente aqui
                                        Text(text = "Caminho da foto (local): ${photoLoc.split('/').lastOrNull() ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                                    }
                                } ?: run {
                                    Text(
                                        text = "Local da Foto: Não informado",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                latestConsultation.notes?.let { notes ->
                                    Text(
                                        text = "Notas: $notes",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                } ?: run {
                                    Text(
                                        text = "Notas: Nenhuma nota registrada",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Text(text = "Nenhuma consulta registrada para este paciente.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        is PatientConsultationsUiState.Error -> {
                            Text(
                                text = (patientConsultationsUiState as PatientConsultationsUiState.Error).message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                        is PatientConsultationsUiState.Idle -> {
                            Text("Aguardando dados da consulta...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                is PatientDataUiState.Error -> {
                    Text(
                        text = (patientUiState as PatientDataUiState.Error).message,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}