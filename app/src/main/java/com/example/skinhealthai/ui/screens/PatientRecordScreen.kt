package com.example.skinhealthai.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

import coil.compose.rememberImagePainter
import com.example.skinhealthai.data.model.AppointmentPdfData
import com.example.skinhealthai.data.model.ConsultationResponse
import com.example.skinhealthai.data.model.MedicalRecordPdfContent
import com.example.skinhealthai.data.model.PatientRecordPdfData
import com.example.skinhealthai.ui.viewmodel.ConsultationViewModel
import com.example.skinhealthai.ui.viewmodel.DeleteConsultationUiState
import com.example.skinhealthai.ui.viewmodel.PatientConsultationsUiState
import com.example.skinhealthai.ui.viewmodel.PatientDataUiState
import com.example.skinhealthai.utils.PdfGenerator
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// --- IMPORTS DO SEU APP ---
import com.example.skinhealthai.viewmodel.ConsultationViewModelFactory
import com.example.skinhealthai.data.network.RetrofitInstance
import com.example.skinhealthai.repository.PatientRepository
import com.example.skinhealthai.repository.ConsultationRepository
import com.example.skinhealthai.repository.FileImageRepository
// NOVO: Imports para os modelos de dados da imagem e análise
import com.example.skinhealthai.data.model.FileImageWithAnalysisResponse
import com.example.skinhealthai.data.model.AnalysisResultData
// NOVO: Import para o modificador clip
import androidx.compose.ui.draw.clip
// Para MaterialTheme.shapes (normalmente já vem, mas bom verificar)
import androidx.compose.material3.MaterialTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRecordScreen(
    navController: NavController,
    patientId: Int?,
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
    val coroutineScope = rememberCoroutineScope()

    val patientUiState by consultationViewModel.patientDataUiState.collectAsState()
    val patientConsultationsUiState by consultationViewModel.patientConsultationsUiState.collectAsState()
    val deleteConsultationState by consultationViewModel.deleteConsultationState.collectAsState()

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var consultationToDelete by remember { mutableStateOf<ConsultationResponse?>(null) }

    var filterText by remember { mutableStateOf("") }


    val sharePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { /* Não precisamos fazer nada com o resultado aqui */ }
    )

    LaunchedEffect(patientId) {
        if (patientId != null) {
            consultationViewModel.loadPatient(patientId)
            consultationViewModel.loadPatientConsultations(patientId)
        }
    }

    LaunchedEffect(deleteConsultationState) {
        val currentDeleteConsultationState = deleteConsultationState // Para smart cast
        when (currentDeleteConsultationState) {
            is DeleteConsultationUiState.Success -> {
                Toast.makeText(context, "Consulta excluída com sucesso!", Toast.LENGTH_SHORT).show()
                consultationViewModel.resetDeleteConsultationState()
                patientId?.let { consultationViewModel.loadPatientConsultations(it) }
            }
            is DeleteConsultationUiState.Error -> {
                val errorMessage = currentDeleteConsultationState.message
                Toast.makeText(context, "Erro ao excluir consulta: $errorMessage", Toast.LENGTH_LONG).show()
                consultationViewModel.resetDeleteConsultationState()
            }
            else -> { /* Não fazer nada para Idle ou Loading */ }
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
                    IconButton(onClick = {
                        navController.navigate(AppRoutes.HOME) {
                            popUpTo(AppRoutes.HOME) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar para Home")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                )
            )
        },

        floatingActionButton = {
            val currentPatientUiState = patientUiState // Para smart cast
            val currentPatientConsultationsUiState = patientConsultationsUiState // Para smart cast

            if (currentPatientUiState is PatientDataUiState.PatientLoaded &&
                currentPatientConsultationsUiState is PatientConsultationsUiState.Loaded) {
                FloatingActionButton(
                    onClick = {
                        val patientData = currentPatientUiState.patient
                        val consultations = currentPatientConsultationsUiState.consultations

                        val patientPdfData = PatientRecordPdfData.fromPatientResponse(patientData)
                        // AQUI: Você precisará adaptar AppointmentPdfData.fromConsultationResponse
                        // para passar as URLs das imagens e os resultados da análise
                        val appointmentsPdfData = consultations.map { AppointmentPdfData.fromConsultationResponse(it) }

                        val medicalRecordContent = MedicalRecordPdfContent(
                            patientData = patientPdfData,
                            appointments = appointmentsPdfData
                        )

                        coroutineScope.launch {
                            val pdfUri = PdfGenerator.generateMedicalRecordPdf(context, medicalRecordContent)
                            if (pdfUri != null) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_STREAM, pdfUri)
                                    type = "application/pdf"
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                sharePdfLauncher.launch(Intent.createChooser(shareIntent, "Compartilhar Prontuário PDF com..."))
                            } else {
                                Toast.makeText(context, "Erro ao gerar PDF do prontuário.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "Gerar PDF do Prontuário")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val currentPatientUiState = patientUiState // Para smart cast
            when (currentPatientUiState) {
                is PatientDataUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    Text("Carregando dados do paciente...", modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is PatientDataUiState.PatientLoaded -> {
                    val patient = currentPatientUiState.patient

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Nome: ${patient.name}",
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

                    patient.gender?.let { genderValue ->
                        val fullGenderText = when(genderValue.uppercase(Locale.getDefault())) {
                            "M", "MASCULINO" -> "Masculino"
                            "F", "FEMININO" -> "Feminino"
                            "O", "OUTRO" -> "Outro"
                            else -> genderValue
                        }
                        Text(
                            text = "Gênero: $fullGenderText",
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
                        val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val apiFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Formato da API

                        val formattedDate = try {
                            apiFormat.parse(apiDateString)?.let { dateObject ->
                                displayFormat.format(dateObject)
                            }
                        } catch (e: ParseException) { null } catch (e: Exception) { null }

                        if (formattedDate != null) {
                            val age = calculateAge(apiDateString, apiFormat)
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

                    patient.cellphone?.let {
                        Text(
                            text = "Telefone: $it",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Histórico de Consultas",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = filterText,
                        onValueChange = { filterText = it },
                        label = { Text("Filtrar por Data (dd/MM/yyyy) ou Local da Lesão") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    val currentPatientConsultationsUiState = patientConsultationsUiState // Para smart cast
                    when (currentPatientConsultationsUiState) {
                        is PatientConsultationsUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            Text("Carregando histórico de consultas...", modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                        is PatientConsultationsUiState.Loaded -> {
                            val allConsultations = currentPatientConsultationsUiState.consultations

                            val filteredConsultations = remember(filterText, allConsultations) {
                                if (filterText.isBlank()) {
                                    allConsultations
                                } else {
                                    val lowerCaseFilter = filterText.lowercase()
                                    allConsultations.filter { consultation ->
                                        val consultationDateString = try {
                                            val apiFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())
                                            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                            apiFormat.parse(consultation.dateConsultation)?.let { displayFormat.format(it) }
                                        } catch (e: Exception) { null }
                                        val matchesDate = consultationDateString?.lowercase()?.contains(lowerCaseFilter) ?: false

                                        val matchesLocation = consultation.photoLocation?.lowercase()?.contains(lowerCaseFilter) ?: false

                                        val matchesNotes = consultation.notes?.lowercase()?.contains(lowerCaseFilter) ?: false

                                        matchesDate || matchesLocation || matchesNotes
                                    }
                                }
                            }

                            val sortedConsultations = filteredConsultations.sortedByDescending {
                                try {
                                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault()).parse(it.dateConsultation) ?: Date(0)
                                } catch (e: Exception) {
                                    Date(0)
                                }
                            }

                            if (sortedConsultations.isEmpty() && filterText.isNotBlank()) {
                                Text(
                                    text = "Nenhuma consulta encontrada com o filtro: \"$filterText\".",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else if (sortedConsultations.isEmpty()) {
                                Text(
                                    text = "Nenhuma consulta registrada para este paciente.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    sortedConsultations.forEach { consultation ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val displayDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                                    val apiDateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())

                                                    val formattedConsultationDate = try {
                                                        apiDateTimeFormat.parse(consultation.dateConsultation)?.let { dateObject ->
                                                            displayDateFormat.format(dateObject)
                                                        }
                                                    } catch (e: ParseException) { null } catch (e: Exception) { null }

                                                    Text(
                                                        text = "Data da Consulta: ${formattedConsultationDate ?: "Não informada"}",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Row(horizontalArrangement = Arrangement.End) {
                                                        IconButton(
                                                            onClick = {
                                                                consultation.id?.let { consId ->
                                                                    patientId?.let { pId ->
                                                                        navController.navigate("${AppRoutes.CONSULTATION_SCREEN_BASE}/${pId}?consultationId=${consId}")
                                                                    } ?: Toast.makeText(context, "Erro: ID do paciente não disponível para edição.", Toast.LENGTH_SHORT).show()
                                                                } ?: Toast.makeText(context, "Erro: ID da consulta não disponível para edição.", Toast.LENGTH_SHORT).show()
                                                            }
                                                        ) {
                                                            Icon(Icons.Default.Edit, contentDescription = "Editar Consulta")
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                consultationToDelete = consultation
                                                                showDeleteConfirmationDialog = true
                                                            }
                                                        ) {
                                                            Icon(Icons.Default.Delete, contentDescription = "Excluir Consulta")
                                                        }
                                                    }
                                                }

                                                // NOVO: Exibir imagens com análise (formato solicitado)
                                                consultation.imagesWithAnalysis?.let { imagesWithAnalysis ->
                                                    if (imagesWithAnalysis.isNotEmpty()) {
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(
                                                            text = "Imagens da Consulta:",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.SemiBold,
                                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        imagesWithAnalysis.forEach { imageWithAnalysis ->
                                                            // Exibe a imagem
                                                            imageWithAnalysis.imageUrl?.let { imageUrl ->
                                                                Image(
                                                                    painter = rememberImagePainter(data = imageUrl),
                                                                    contentDescription = "Imagem da Consulta",
                                                                    modifier = Modifier
                                                                        .size(150.dp)
                                                                        .clip(MaterialTheme.shapes.medium)
                                                                        .align(Alignment.CenterHorizontally),
                                                                    contentScale = ContentScale.Crop
                                                                )
                                                                Spacer(modifier = Modifier.height(8.dp))
                                                            }

                                                            // Exibe o resultado da predição, se houver
                                                            imageWithAnalysis.analysisResult?.let { analysis ->
                                                                // CORREÇÃO: Envolver composables em um Column ou Box
                                                                Column(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                                ) {
                                                                    analysis.result?.let { pred ->
                                                                        val conf = String.format(Locale.getDefault(), "%.2f", analysis.confidence?.times(100) ?: 0.0)
                                                                        Text(
                                                                            text = "Predição IA: $pred (Confiança: $conf%)",
                                                                            style = MaterialTheme.typography.bodySmall,
                                                                            color = MaterialTheme.colorScheme.secondary
                                                                        )
                                                                    } ?: analysis.error?.let { errorMsg ->
                                                                        Text(
                                                                            text = "Erro na IA: $errorMsg",
                                                                            style = MaterialTheme.typography.bodySmall,
                                                                            color = MaterialTheme.colorScheme.error // MaterialTheme.colorScheme.error é correto
                                                                        )
                                                                    }
                                                                    Spacer(modifier = Modifier.height(8.dp)) // Espaçador após o resultado da predição
                                                                }
                                                            }
                                                            // Adiciona um espaçamento maior entre imagens/análises se houver mais de uma
                                                            if (imagesWithAnalysis.size > 1) {
                                                                Spacer(modifier = Modifier.height(16.dp))
                                                            }
                                                        }
                                                    }
                                                }

                                                consultation.notes?.let { notes ->
                                                    Text(
                                                        text = "Notas: $notes",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    )
                                                }
                                                consultation.photoLocation?.let { photoLoc ->
                                                    Text(
                                                        text = "Local da Foto: $photoLoc",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        is PatientConsultationsUiState.Error -> {
                            Text(
                                text = (currentPatientConsultationsUiState as PatientConsultationsUiState.Error).message, // Smart cast for error
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                        is PatientConsultationsUiState.Idle -> {
                            Text("Aguardando histórico de consultas...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                is PatientDataUiState.Error -> {
                    Text(
                        text = (currentPatientUiState as PatientDataUiState.Error).message, // Smart cast for error
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

    if (showDeleteConfirmationDialog && consultationToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmationDialog = false
                consultationToDelete = null
            },
            title = { Text("Confirmar Exclusão da Consulta") },
            text = {
                Text(
                    "Tem certeza que deseja excluir esta consulta (ID: ${consultationToDelete?.id}) " +
                            "do paciente ${consultationToDelete?.patientDetails?.name ?: "Desconhecido"}?\n\n" +
                            "Esta ação é irreversível."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        consultationToDelete?.id?.let { consId ->
                            consultationViewModel.deleteConsultation(consId)
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

fun calculateAge(dobString: String, dateFormat: SimpleDateFormat): String? {
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