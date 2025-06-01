package com.example.skinhealthai.ui.screens

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
import coil.compose.rememberImagePainter
import com.example.skinhealthai.ui.viewmodel.ConsultationViewModel
import com.example.skinhealthai.ui.viewmodel.PatientDataUiState
import com.example.skinhealthai.ui.viewmodel.PatientConsultationsUiState
import com.example.skinhealthai.data.model.ConsultationResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.text.ParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRecordScreen(
    navController: NavController,
    patientId: Int?,
    consultationId: Int? = null,
    consultationViewModel: ConsultationViewModel = viewModel()
) {

    val patientUiState by consultationViewModel.patientDataUiState.collectAsState()
    val patientConsultationsUiState by consultationViewModel.patientConsultationsUiState.collectAsState()

    LaunchedEffect(patientId) {
        if (patientId != null) {
            consultationViewModel.loadPatient(patientId)
            consultationViewModel.loadPatientConsultations(patientId)
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (patientUiState) {
                is PatientDataUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    Text("Carregando dados do paciente...", modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is PatientDataUiState.PatientLoaded -> {
                    val patient = (patientUiState as PatientDataUiState.PatientLoaded).patient

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Nome: ${patient.name}",
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

                    patient.date_of_birth?.let { apiDateString ->
                        val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val apiFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                        val formattedDate = try {
                            apiFormat.parse(apiDateString)?.let { dateObject ->
                                displayFormat.format(dateObject)
                            }
                        } catch (e: ParseException) {
                            null
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
                    patient.cellphone?.let {
                        Text(
                            text = "Telefone: $it",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Dados da Consulta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                    when (patientConsultationsUiState) {
                        is PatientConsultationsUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            Text("Carregando dados da consulta...", modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                        is PatientConsultationsUiState.Loaded -> {
                            val consultations = (patientConsultationsUiState as PatientConsultationsUiState.Loaded).consultations

                            val consultationToDisplay: ConsultationResponse? = if (consultationId != null) {
                                consultations.firstOrNull { it.id == consultationId }
                            } else {
                                consultations.maxByOrNull {
                                    try {
                                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault()).parse(it.dateConsultation) ?: Date(0) // Usar X para Z
                                    } catch (e: Exception) {
                                        Date(0)
                                    }
                                }
                            }

                            if (consultationToDisplay != null) {
                                val displayDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                val apiDateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault()) // Adicionado 'X' para o fuso horário Z

                                val formattedConsultationDate = try {
                                    apiDateTimeFormat.parse(consultationToDisplay.dateConsultation)?.let { dateObject ->
                                        displayDateFormat.format(dateObject)
                                    }
                                } catch (e: ParseException) {
                                    null
                                } catch (e: Exception) {
                                    null
                                }

                                Text(
                                    text = "Data da Consulta: ${formattedConsultationDate ?: "Não informada"}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                consultationToDisplay.photoLocation?.let { photoLoc ->
                                    Text(
                                        text = "Local da Foto: $photoLoc",
                                        style = MaterialTheme.typography.bodyLarge
                                    )

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
                                        Text(text = "Caminho da foto (local): ${photoLoc.split('/').lastOrNull() ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                                    }
                                } ?: run {
                                    Text(
                                        text = "Local da Foto: Não informado",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                consultationToDisplay.notes?.let { notes ->
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
                                Text(text = "Nenhuma consulta encontrada para este paciente ou a consulta selecionada não existe.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                is PatientDataUiState.Idle -> {
                    Text("Aguardando o carregamento dos dados do paciente...", modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }
}