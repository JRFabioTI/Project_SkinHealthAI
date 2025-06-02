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
import androidx.navigation.NavController
import com.example.skinhealthai.data.model.ConsultationRequest
import com.example.skinhealthai.ui.viewmodel.ConsultationViewModel
import com.example.skinhealthai.ui.viewmodel.PatientDataUiState
import com.example.skinhealthai.ui.viewmodel.ConsultationCreationState
import com.example.skinhealthai.ui.viewmodel.SingleConsultationUiState
import com.example.skinhealthai.ui.viewmodel.UpdateConsultationUiState
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar // Importar Calendar para cálculo de idade
import java.util.Date
import java.util.Locale

// Certifique-se de ter importado AppRoutes
import com.example.skinhealthai.ui.screens.AppRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationScreen(
    navController: NavController,
    patientId: Int?,
    consultationId: Int? = null,
    consultationViewModel: ConsultationViewModel = viewModel()
) {
    val context = LocalContext.current
    val isEditing = consultationId != null

    var consultationDate by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())) }
    var photoLocationDescription by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val patientUiState by consultationViewModel.patientDataUiState.collectAsState()
    val consultationCreationState by consultationViewModel.consultationCreationState.collectAsState()
    val singleConsultationUiState by consultationViewModel.singleConsultationUiState.collectAsState()
    val updateConsultationState by consultationViewModel.updateConsultationState.collectAsState()

    LaunchedEffect(patientId, consultationId) {
        if (patientId != null) {
            consultationViewModel.loadPatient(patientId)
        } else {
            Toast.makeText(context, "Erro: ID do paciente não fornecido.", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }

        if (isEditing && consultationId != null) {
            consultationViewModel.loadSingleConsultation(consultationId)
        }
    }

    LaunchedEffect(singleConsultationUiState) {
        when (singleConsultationUiState) {
            is SingleConsultationUiState.Loaded -> {
                val consultation = (singleConsultationUiState as SingleConsultationUiState.Loaded).consultation
                try {
                    val apiDateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())
                    val displayFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val dateObject = apiDateTimeFormat.parse(consultation.dateConsultation)
                    consultationDate = dateObject?.let { displayFormat.format(it) } ?: ""
                } catch (e: Exception) {
                    consultationDate = ""
                    Toast.makeText(context, "Erro ao formatar data da consulta existente.", Toast.LENGTH_SHORT).show()
                }
                photoLocationDescription = consultation.photoLocation ?: ""
                notes = consultation.notes ?: ""
                consultationViewModel.resetSingleConsultationState()
            }
            is SingleConsultationUiState.Error -> {
                Toast.makeText(context, (singleConsultationUiState as SingleConsultationUiState.Error).message, Toast.LENGTH_LONG).show()
                consultationViewModel.resetSingleConsultationState()
                navController.popBackStack()
            }
            else -> { /* Loading ou Idle, não fazer nada */ }
        }
    }

    LaunchedEffect(consultationCreationState, updateConsultationState) {
        when (consultationCreationState) {
            is ConsultationCreationState.Success -> {
                Toast.makeText(context, "Consulta registrada com sucesso!", Toast.LENGTH_SHORT).show()
                consultationViewModel.resetConsultationCreationState()
                navController.navigate("${AppRoutes.PATIENT_RECORD_BASE}/${patientId}") {
                    popUpTo("${AppRoutes.PATIENT_RECORD_BASE}/${patientId}") { inclusive = true }
                }
            }
            is ConsultationCreationState.Error -> {
                Toast.makeText(context, (consultationCreationState as ConsultationCreationState.Error).message, Toast.LENGTH_LONG).show()
                consultationViewModel.resetConsultationCreationState()
            }
            else -> { /* Idle ou Loading */ }
        }

        when (updateConsultationState) {
            is UpdateConsultationUiState.Success -> {
                Toast.makeText(context, "Consulta atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                consultationViewModel.resetUpdateConsultationState()
                navController.navigate("${AppRoutes.PATIENT_RECORD_BASE}/${patientId}") {
                    popUpTo("${AppRoutes.PATIENT_RECORD_BASE}/${patientId}") { inclusive = true }
                }
            }
            is UpdateConsultationUiState.Error -> {
                Toast.makeText(context, (updateConsultationState as UpdateConsultationUiState.Error).message, Toast.LENGTH_LONG).show()
                consultationViewModel.resetUpdateConsultationState()
            }
            else -> { /* Idle ou Loading */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Consulta" else "Registrar Nova Consulta", fontWeight = FontWeight.Bold) },
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
                .imePadding(),
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
                        text = "Paciente: ${patient.name}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    patient.email?.let {
                        Text(
                            text = "Email: $it",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    patient.cpf?.let {
                        Text(
                            text = "CPF: $it",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // --- EXIBIR GÊNERO E IDADE ---
                    patient.gender?.let { genderCode ->
                        val fullGender = when(genderCode.uppercase(Locale.getDefault())) {
                            "M", "MASCULINO" -> "Masculino"
                            "F", "FEMININO" -> "Feminino"
                            "O", "OUTRO" -> "Outro"
                            else -> genderCode // Se não for reconhecido, mostra o valor original
                        }
                        Text(
                            text = "Gênero: $fullGender",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } ?: run {
                        Text(
                            text = "Gênero: Não informado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    patient.date_of_birth?.let { apiDateString ->
                        val apiDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Formato que a API retorna
                        val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Formato para exibição

                        val formattedDate = try {
                            apiDateFormat.parse(apiDateString)?.let { dateObject ->
                                displayDateFormat.format(dateObject)
                            }
                        } catch (e: ParseException) { null } catch (e: Exception) { null }

                        if (formattedDate != null) {
                            val age = calculateAge2(apiDateString, apiDateFormat) // Usar apiDateFormat para cálculo
                            Text(
                                text = "Data de Nascimento: $formattedDate (Idade: ${age ?: "N/A"})",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Text(
                                text = "Data de Nascimento: Não informada",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } ?: run {
                        Text(
                            text = "Data de Nascimento: Não informada",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // --- FIM EXIBIR GÊNERO E IDADE ---

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isEditing) "Dados para Edição" else "Dados da Consulta",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = consultationDate,
                        onValueChange = { consultationDate = it },
                        label = { Text("Data e Hora da Consulta (dd/MM/yyyy HH:mm)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = photoLocationDescription,
                        onValueChange = { photoLocationDescription = it },
                        label = { Text("Local da Lesão") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Descrição da Lesão / Notas da Consulta") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 130.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (patientId == null) {
                                Toast.makeText(context, "Erro: Paciente não identificado.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val apiDateTimeFormat = "yyyy-MM-dd'T'HH:mm:ssZ" // Ajuste para 'X' se necessário
                            val formattedConsultationDate: String? = try {
                                val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                inputFormat.isLenient = false
                                val date = inputFormat.parse(consultationDate)
                                SimpleDateFormat(apiDateTimeFormat, Locale.getDefault()).format(date)
                            } catch (e: ParseException) {
                                Toast.makeText(context, "Formato de data/hora inválido. Use dd/MM/yyyy HH:mm", Toast.LENGTH_LONG).show()
                                null
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erro ao processar data/hora da consulta: ${e.message}", Toast.LENGTH_LONG).show()
                                null
                            }

                            if (formattedConsultationDate == null) {
                                return@Button
                            }

                            val consultationRequest = ConsultationRequest(
                                patientId = patient.id,
                                dateConsultation = formattedConsultationDate,
                                photoLocation = photoLocationDescription.takeIf { it.isNotBlank() },
                                notes = notes.takeIf { it.isNotBlank() }
                            )

                            if (isEditing && consultationId != null) {
                                consultationViewModel.updateConsultation(consultationId, consultationRequest)
                            } else {
                                consultationViewModel.createConsultation(consultationRequest)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .align(Alignment.CenterHorizontally),
                        enabled = (consultationCreationState !is ConsultationCreationState.Loading &&
                                updateConsultationState !is UpdateConsultationUiState.Loading &&
                                singleConsultationUiState !is SingleConsultationUiState.Loading)
                    ) {
                        if (consultationCreationState is ConsultationCreationState.Loading ||
                            updateConsultationState is UpdateConsultationUiState.Loading ||
                            singleConsultationUiState is SingleConsultationUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (isEditing) "Atualizar" else "Salvar")
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
                    Text("Aguardando dados do paciente...", modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }
}

// --- Função auxiliar para calcular idade (se não estiver já em um arquivo utilitário) ---
fun calculateAge2(dobString: String, dateFormat: SimpleDateFormat): String? {
    return try {
        val dob = dateFormat.parse(dobString) ?: return null
        val dobCalendar = Calendar.getInstance().apply { time = dob }
        val todayCalendar = Calendar.getInstance()

        var age = todayCalendar.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)
        if (todayCalendar.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        "$age anos"
    } catch (e: Exception) {
        null
    }
}