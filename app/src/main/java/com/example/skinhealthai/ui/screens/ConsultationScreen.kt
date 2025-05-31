package com.example.skinhealthai.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.skinhealthai.data.model.ConsultationRequest
// Importe o ViewModel renomeado
import com.example.skinhealthai.ui.viewmodel.ConsultationViewModel
import com.example.skinhealthai.ui.viewmodel.PatientDataUiState // Use o estado renomeado
import com.example.skinhealthai.ui.viewmodel.ConsultationCreationState
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationScreen(
    navController: NavHostController,
    patientId: Int?, // Recebe o ID do paciente
    consultationViewModel: ConsultationViewModel = viewModel() // Use o ViewModel renomeado
) {
    val context = LocalContext.current

    // Estados para os campos da consulta
    // Inicializa com a data/hora atual formatada
    var consultationDate by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())) }
    // Preenche com "Imagem capturada" se houver uma imagem no ImageUploadViewModel
    var notes by remember { mutableStateOf("") }

    // Observe o estado do carregamento do paciente
    val patientUiState by consultationViewModel.patientDataUiState.collectAsState()
    // Observe o estado da criação da consulta
    val consultationCreationState by consultationViewModel.consultationCreationState.collectAsState()

    // Acionar o carregamento do paciente quando a tela é iniciada
    LaunchedEffect(patientId) {
        if (patientId != null) {
            consultationViewModel.loadPatient(patientId)
        } else {
            Toast.makeText(context, "Erro: ID do paciente não fornecido.", Toast.LENGTH_SHORT).show()
            navController.popBackStack() // Volta se não tiver ID
        }
    }

    // Reagir ao estado de criação da consulta
    LaunchedEffect(consultationCreationState) {
        when (consultationCreationState) {
            is ConsultationCreationState.Success -> {
                Toast.makeText(context, "Consulta registrada com sucesso!", Toast.LENGTH_SHORT).show()
                // Limpar campos após o sucesso
                consultationDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                photoLocation = ""
                notes = ""
                consultationViewModel.resetConsultationCreationState()
                // Opcional: Navegar de volta ou para a tela de prontuário após salvar
                navController.popBackStack()
            }
            is ConsultationCreationState.Error -> {
                Toast.makeText(context, (consultationCreationState as ConsultationCreationState.Error).message, Toast.LENGTH_LONG).show()
                consultationViewModel.resetConsultationCreationState()
            }
            else -> {} // Idle ou Loading, nenhuma ação específica aqui
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Nova Consulta", fontWeight = FontWeight.Bold) },
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
                .imePadding(), // Adicionado para ajustar o layout quando o teclado aparece
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Exibir informações do paciente
            when (patientUiState) {
                is PatientDataUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    Text("Carregando dados do paciente...", modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is PatientDataUiState.PatientLoaded -> {
                    val patient = (patientUiState as PatientDataUiState.PatientLoaded).patient
                    Text(
                        text = "Paciente: ${patient.name}",
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
                    patient.date_of_birth?.let {
                        Text(
                            text = "Data de Nascimento: $it", // Formato YYYY-MM-DD
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } ?: run {
                        Text(
                            text = "Data de Nascimento: Não informada",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Dados da Consulta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Campos de input para a consulta
                    OutlinedTextField(
                        value = consultationDate,
                        onValueChange = { consultationDate = it },
                        label = { Text("Data e Hora da Consulta (dd/MM/yyyy HH:mm)") },
                        singleLine = true,
                        // Pode usar KeyboardType.Text ou implementar máscara para data/hora
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = photoLocation,
                        onValueChange = { photoLocation = it },
                        label = { Text("Local da Foto / Descrição da Imagem") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas da Consulta") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp) // Altura mínima para notas
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (patientId == null) {
                                Toast.makeText(context, "Erro: Paciente não identificado.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Validação e formatação da data e hora da consulta para a API
                            val apiDateTimeFormat = "yyyy-MM-dd'T'HH:mm:ssZ" // Formato ISO 8601 com fuso horário
                            val formattedConsultationDate: String? = try {
                                val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                inputFormat.isLenient = false // Garante validação rigorosa
                                val date = inputFormat.parse(consultationDate) // Tenta parsear
                                SimpleDateFormat(apiDateTimeFormat, Locale.getDefault()).format(date) // Formata para a API
                            } catch (e: ParseException) {
                                Toast.makeText(context, "Formato de data/hora inválido. Use dd/MM/yyyy HH:mm", Toast.LENGTH_LONG).show()
                                null
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erro ao processar data/hora da consulta.", Toast.LENGTH_LONG).show()
                                null
                            }

                            if (formattedConsultationDate == null) {
                                return@Button // Não prossegue se a data for inválida
                            }

                            val consultationRequest = ConsultationRequest(
                                patientId = patient.id, // ID do paciente carregado
                                dateConsultation = formattedConsultationDate,
                                photoLocation = photoLocation.takeIf { it.isNotBlank() },
                                notes = notes.takeIf { it.isNotBlank() }
                            )
                            consultationViewModel.createConsultation(consultationRequest)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = consultationCreationState !is ConsultationCreationState.Loading
                    ) {
                        if (consultationCreationState is ConsultationCreationState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Registrar Consulta")
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

