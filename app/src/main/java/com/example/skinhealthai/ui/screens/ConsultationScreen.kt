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
// FileUtils e classes relacionadas à câmera não são mais necessários se não for tirar foto aqui
// import com.example.skinhealthai.utils.FileUtils
// import android.graphics.Bitmap
// import android.net.Uri
// import androidx.core.content.FileProvider
// import android.Manifest
// import android.content.pm.PackageManager
// import androidx.activity.compose.rememberLauncherForActivityResult
// import androidx.activity.result.contract.ActivityResultContracts
// import androidx.core.content.ContextCompat

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Removida a assunção de CameraCapture, pois não será usada
// @Composable
// fun CameraCapture(onImageCaptured: (Bitmap?) -> Unit) { /* ... */ }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationScreen(
    navController: NavController,
    patientId: Int?,
    consultationViewModel: ConsultationViewModel = viewModel()
) {
    val context = LocalContext.current

    // Estados para os campos da consulta
    var consultationDate by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())) }
    // photoUri não é mais necessário aqui se a foto não for tirada nesta tela
    // var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoLocationDescription by remember { mutableStateOf("") } // Estado para a descrição do local da foto
    var notes by remember { mutableStateOf("") }

    // Estados e Launchers de câmera removidos
    // var showCamera by remember { mutableStateOf(false) }
    // val cameraPermissionState = remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    // val permissionLauncher = rememberLauncherForActivityResult(...)

    // Observa os estados do ViewModel
    val patientUiState by consultationViewModel.patientDataUiState.collectAsState()
    val consultationCreationState by consultationViewModel.consultationCreationState.collectAsState()

    // Carrega os dados do paciente ao iniciar a tela
    LaunchedEffect(patientId) {
        if (patientId != null) {
            consultationViewModel.loadPatient(patientId)
        } else {
            Toast.makeText(context, "Erro: ID do paciente não fornecido.", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    // Reage ao estado de criação da consulta
    LaunchedEffect(consultationCreationState) {
        when (consultationCreationState) {
            is ConsultationCreationState.Success -> {
                Toast.makeText(context, "Consulta registrada com sucesso!", Toast.LENGTH_SHORT).show()
                // Limpa os campos após o sucesso
                consultationDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                // photoUri = null // Não necessário
                photoLocationDescription = "" // Limpa o campo
                notes = ""
                // Navega para a tela de prontuário, mas agora SEM a photoUri
                navController.navigate("${AppRoutes.PATIENT_RECORD_BASE}/${patientId}") // <-- Rota modificada
                consultationViewModel.resetConsultationCreationState()
            }
            is ConsultationCreationState.Error -> {
                Toast.makeText(context, (consultationCreationState as ConsultationCreationState.Error).message, Toast.LENGTH_LONG).show()
                consultationViewModel.resetConsultationCreationState()
            }
            else -> {}
        }
    }

    // O bloco 'if (showCamera)' e 'else' foi removido, agora é sempre o conteúdo da tela de consulta
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
                .imePadding(),
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
                    patient.email?.let { Text(text = "Email: $it", style = MaterialTheme.typography.bodyLarge) }
                    patient.cpf?.let { Text(text = "CPF: $it", style = MaterialTheme.typography.bodyLarge) }
                    patient.date_of_birth?.let { Text(text = "Data de Nascimento: $it", style = MaterialTheme.typography.bodyLarge) }
                        ?: run { Text(text = "Data de Nascimento: Não informada", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Dados da Consulta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Campos de input para a consulta
                    OutlinedTextField(
                        value = consultationDate,
                        onValueChange = { consultationDate = it },
                        label = { Text("Data e Hora da Consulta (dd/MM/yyyy HH:mm)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Botão "Tirar Foto" removido
                    // Button(onClick = { ... }) { Text("Tirar Foto") }

                    // Texto "Foto capturada: ..." removido
                    // photoUri?.let { uri -> Text("Foto capturada: ${uri.lastPathSegment ?: "Imagem"}", style = MaterialTheme.typography.bodyMedium) }

                    // Campo para a descrição do local da foto
                    OutlinedTextField(
                        value = photoLocationDescription,
                        onValueChange = { photoLocationDescription = it },
                        label = { Text("Local da Foto / Descrição da Lesão (Opcional)") }, // Label mais descritivo e indica opcional
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas da Consulta") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (patientId == null) {
                                Toast.makeText(context, "Erro: Paciente não identificado.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val apiDateTimeFormat = "yyyy-MM-dd'T'HH:mm:ssZ"
                            val formattedConsultationDate: String? = try {
                                val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                inputFormat.isLenient = false
                                val date = inputFormat.parse(consultationDate)
                                SimpleDateFormat(apiDateTimeFormat, Locale.getDefault()).format(date)
                            } catch (e: ParseException) {
                                Toast.makeText(context, "Formato de data/hora inválido. Use dd/MM/yyyy HH:mm", Toast.LENGTH_LONG).show()
                                null
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erro ao processar data/hora da consulta.", Toast.LENGTH_LONG).show()
                                null
                            }

                            if (formattedConsultationDate == null) {
                                return@Button
                            }

                            val consultationRequest = ConsultationRequest(
                                patientId = patient.id,
                                dateConsultation = formattedConsultationDate,
                                // Envia a descrição do local da foto apenas se não estiver vazia
                                photoLocation = photoLocationDescription.takeIf { it.isNotBlank() }, // Não usa photoUri
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