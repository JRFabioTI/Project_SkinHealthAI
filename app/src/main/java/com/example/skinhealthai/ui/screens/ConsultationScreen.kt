package com.example.skinhealthai.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.navigation.NavOptionsBuilder
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberImagePainter

import com.example.skinhealthai.data.model.ConsultationRequest
import com.example.skinhealthai.ui.viewmodel.ImageUploadState
import com.example.skinhealthai.ui.viewmodel.ConsultationViewModel
import com.example.skinhealthai.ui.viewmodel.PatientDataUiState
import com.example.skinhealthai.ui.viewmodel.ConsultationCreationState
import com.example.skinhealthai.ui.viewmodel.SingleConsultationUiState
import com.example.skinhealthai.ui.viewmodel.UpdateConsultationUiState
import com.example.skinhealthai.data.network.RetrofitInstance
import com.example.skinhealthai.repository.PatientRepository
import com.example.skinhealthai.repository.ConsultationRepository
import com.example.skinhealthai.repository.FileImageRepository
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.example.skinhealthai.utils.FileUtils
import com.example.skinhealthai.viewmodel.ConsultationViewModelFactory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationScreen(
    navController: NavController,
    patientId: Int?,
    consultationId: Int? = null,
    consultationViewModel: ConsultationViewModel = viewModel(
        factory = ConsultationViewModelFactory(
            apiService = RetrofitInstance.api,
            patientRepository = PatientRepository(),
            consultationRepository = ConsultationRepository(),
            fileImageRepository = FileImageRepository()
        )
    )
) {
    val context = LocalContext.current
    val isEditing = consultationId != null

    var consultationDate by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())) }
    var photoLocationDescription by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var showCamera by remember { mutableStateOf(false) }
    var capturedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentConsultationId by remember { mutableStateOf(consultationId) }

    val existingImageUrls by consultationViewModel.existingImageUrls.collectAsState()

    var currentImageToDisplay by remember { mutableStateOf<Any?>(null) }
    var currentPredictionText by remember { mutableStateOf<String?>(null) }
    var currentPredictionConfidence by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(existingImageUrls, capturedImageBitmap) {
        if (capturedImageBitmap != null) {
            currentImageToDisplay = capturedImageBitmap
            currentPredictionText = null
            currentPredictionConfidence = null
        } else if (existingImageUrls.isNotEmpty()) {
            val firstExistingImage = existingImageUrls.first()
            currentImageToDisplay = firstExistingImage.imageUrl
            currentPredictionText = firstExistingImage.analysisResult?.result
            currentPredictionConfidence = firstExistingImage.analysisResult?.confidence
        } else {
            currentImageToDisplay = null
            currentPredictionText = null
            currentPredictionConfidence = null
        }
    }


    val patientUiState by consultationViewModel.patientDataUiState.collectAsState()
    val consultationCreationState by consultationViewModel.consultationCreationState.collectAsState()
    val singleConsultationUiState by consultationViewModel.singleConsultationUiState.collectAsState()
    val updateConsultationState by consultationViewModel.updateConsultationState.collectAsState()
    val imageUploadState by consultationViewModel.imageUploadState.collectAsState()


    LaunchedEffect(patientId, consultationId) {
        if (patientId != null) {
            consultationViewModel.loadPatient(patientId)
        } else {
            Toast.makeText(context, "Erro: ID do paciente não fornecido.", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }

        if (isEditing && consultationId != null) {
            consultationViewModel.loadSingleConsultation(consultationId)
        } else {
            consultationViewModel.resetExistingImageUrls()
            capturedImageBitmap = null
        }
    }

    LaunchedEffect(singleConsultationUiState) {
        val currentSingleConsultationUiState = singleConsultationUiState
        when (currentSingleConsultationUiState) {
            is SingleConsultationUiState.Loaded -> {
                val consultation = currentSingleConsultationUiState.consultation
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
                currentConsultationId = consultation.id
                consultationViewModel.resetSingleConsultationState()
            }
            is SingleConsultationUiState.Error -> {
                Toast.makeText(context, currentSingleConsultationUiState.message, Toast.LENGTH_LONG).show()
                consultationViewModel.resetSingleConsultationState()
                navController.popBackStack()
            }
            else -> { /* Loading ou Idle, não fazer nada */ }
        }
    }

    LaunchedEffect(consultationCreationState, updateConsultationState) {
        var consultationProcessed = false
        var updateProcessed = false
        var consultationIdForUpload: Int? = null

        val currentConsultationCreationState = consultationCreationState
        when (currentConsultationCreationState) {
            is ConsultationCreationState.Success -> {
                val createdConsultation = currentConsultationCreationState.consultation
                consultationIdForUpload = createdConsultation.id
                Toast.makeText(context, "Consulta registrada com sucesso!", Toast.LENGTH_SHORT).show()
                consultationProcessed = true
                consultationViewModel.resetConsultationCreationState()
            }
            is ConsultationCreationState.Error -> {
                Toast.makeText(context, currentConsultationCreationState.message, Toast.LENGTH_LONG).show()
                consultationProcessed = false
                consultationViewModel.resetConsultationCreationState()
            }
            else -> { /* Loading ou Idle, não fazer nada */ }
        }

        val currentUpdateConsultationState = updateConsultationState
        when (currentUpdateConsultationState) {
            is UpdateConsultationUiState.Success -> {
                val updatedConsultation = currentUpdateConsultationState.consultation
                consultationIdForUpload = updatedConsultation.id
                Toast.makeText(context, "Consulta atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                updateProcessed = true
                consultationViewModel.resetUpdateConsultationState()
            }
            is UpdateConsultationUiState.Error -> {
                Toast.makeText(context, currentUpdateConsultationState.message, Toast.LENGTH_LONG).show()
                updateProcessed = false
                consultationViewModel.resetUpdateConsultationState()
            }
            else -> { /* Loading ou Idle, não fazer nada */ }
        }

        if ((consultationProcessed || updateProcessed) && capturedImageBitmap != null && consultationIdForUpload != null) {
            consultationViewModel.uploadImageForConsultation(capturedImageBitmap!!, consultationIdForUpload)
        } else if (consultationProcessed || updateProcessed) {
            navController.navigate("${AppRoutes.PATIENT_RECORD_BASE}/${patientId}") {
                popUpTo(AppRoutes.PATIENT_RECORD_WITH_PATIENT_ID) { inclusive = true }
            }
        }
    }

    LaunchedEffect(imageUploadState) {
        val currentImageUploadState = imageUploadState
        when (currentImageUploadState) {
            is ImageUploadState.Success -> {
                val uploadedFile = currentImageUploadState.uploadedFile

                val baseMessage = "Upload de imagem concluído!"

                val predictionMessage = uploadedFile.analysisResult?.result?.let { pred ->
                    val conf = String.format(Locale.getDefault(), "%.2f", uploadedFile.analysisResult.confidence?.times(100) ?: 0.0)
                    "\nPredição da IA: $pred (Confiança: $conf%)"
                } ?: uploadedFile.analysisResult?.error?.let { errorMsg ->
                    "\nErro da IA: $errorMsg"
                } ?: "\nPredição da IA não disponível."


                val finalToastMessage = "$baseMessage$predictionMessage"
                Toast.makeText(context, finalToastMessage, Toast.LENGTH_LONG).show()


                consultationViewModel.resetImageUploadState()
                capturedImageBitmap = null

                currentImageToDisplay = uploadedFile.imageUrl
                currentPredictionText = uploadedFile.analysisResult?.result
                currentPredictionConfidence = uploadedFile.analysisResult?.confidence

                 navController.navigate("${AppRoutes.PATIENT_RECORD_BASE}/${patientId}") {
                     popUpTo(AppRoutes.PATIENT_RECORD_WITH_PATIENT_ID) { inclusive = true }
                 }
            }
            is ImageUploadState.Error -> {
                Toast.makeText(context, currentImageUploadState.message, Toast.LENGTH_LONG).show()
                consultationViewModel.resetImageUploadState()
                currentImageToDisplay = null
                currentPredictionText = null
                currentPredictionConfidence = null

                navController.navigate("${AppRoutes.PATIENT_RECORD_BASE}/${patientId}") {
                    popUpTo(AppRoutes.PATIENT_RECORD_WITH_PATIENT_ID) { inclusive = true }
                }
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                )
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .imePadding()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val currentPatientUiState = patientUiState
            when (currentPatientUiState) {
                is PatientDataUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    Text("Carregando dados do paciente...", modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is PatientDataUiState.PatientLoaded -> {
                    val patient = currentPatientUiState.patient

                    Spacer(modifier = Modifier.height(16.dp))

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

                    patient.gender?.let { genderCode ->
                        val fullGender = when(genderCode.uppercase(Locale.getDefault())) {
                            "M", "MASCULINO" -> "Masculino"
                            "F", "FEMININO" -> "Feminino"
                            "O", "OUTRO" -> "Outro"
                            else -> genderCode
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
                        val apiDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                        val formattedDate = try {
                            apiDateFormat.parse(apiDateString)?.let { dateObject ->
                                displayDateFormat.format(dateObject)
                            }
                        } catch (e: ParseException) { null } catch (e: Exception) { null }

                        if (formattedDate != null) {
                            val age = calculateAge2(apiDateString, apiDateFormat)
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

                    // NOVO: Bloco de exibição da IMAGEM: prioriza a capturada, senão mostra a primeira existente
                    val imageToDisplay = capturedImageBitmap ?: existingImageUrls.firstOrNull()

                    imageToDisplay?.let { source ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (source is Bitmap) "Nova Imagem Capturada!" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        when (source) {
                            is Bitmap -> {
                                Image(
                                    bitmap = source.asImageBitmap(),
                                    contentDescription = "Imagem da Consulta",
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                        .align(Alignment.CenterHorizontally),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            is String -> {
                                Image(
                                    painter = rememberImagePainter(data = source),
                                    contentDescription = "Imagem da Consulta",
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                        .align(Alignment.CenterHorizontally),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // NOVO: Exibir resultado da predição abaixo da imagem
                        currentPredictionText?.let { pred ->
                            val conf = String.format(Locale.getDefault(), "%.2f", currentPredictionConfidence?.times(100) ?: 0.0)
                            Text(
                                text = "Predição: $pred (Confiança: $conf%)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }


                    Button(
                        onClick = { showCamera = true },
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .align(Alignment.CenterHorizontally),
                        enabled = imageUploadState !is ImageUploadState.Loading
                    ) {
                        Text("Abrir Câmera")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

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
                                singleConsultationUiState !is SingleConsultationUiState.Loading &&
                                imageUploadState !is ImageUploadState.Loading)
                    ) {
                        if (consultationCreationState is ConsultationCreationState.Loading ||
                            updateConsultationState is UpdateConsultationUiState.Loading ||
                            singleConsultationUiState is SingleConsultationUiState.Loading ||
                            imageUploadState is ImageUploadState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (isEditing) "Atualizar" else "Salvar")
                        }
                    }
                }
                is PatientDataUiState.Error -> {
                    Text(
                        text = (currentPatientUiState as PatientDataUiState.Error).message,
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

    if (showCamera) {
        CameraCapture(
            onImageCaptured = { bitmap ->
                showCamera = false
                capturedImageBitmap = bitmap

                if (bitmap != null) {
                    val savedUri = FileUtils.saveBitmapToGallery(context, bitmap)
                    if (savedUri != null) {
                        Toast.makeText(context, "Imagem salva na galeria!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Falha ao salvar imagem na galeria.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Captura de imagem cancelada ou falhou.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}


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
