package com.example.skinhealthai.ui.screens

import android.net.Uri
import android.widget.Toast // Importar Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete // Importar ícone de exclusão
import androidx.compose.material.icons.filled.Edit // Importar ícone de edição
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // Importar LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.skinhealthai.ui.viewmodel.ConsultationViewModel
import com.example.skinhealthai.ui.viewmodel.PatientDataUiState
import com.example.skinhealthai.ui.viewmodel.PatientConsultationsUiState
import com.example.skinhealthai.data.model.ConsultationResponse
import com.example.skinhealthai.ui.viewmodel.DeleteConsultationUiState // --- IMPORT AQUI ---
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.text.ParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRecordScreen(
    navController: NavController,
    patientId: Int?,
    consultationViewModel: ConsultationViewModel = viewModel()
) {
    val context = LocalContext.current

    val patientUiState by consultationViewModel.patientDataUiState.collectAsState()
    val patientConsultationsUiState by consultationViewModel.patientConsultationsUiState.collectAsState()
    val deleteConsultationState by consultationViewModel.deleteConsultationState.collectAsState() // --- OBSERVAR ESTADO ---

    // Estados para o diálogo de confirmação de exclusão
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var consultationToDelete by remember { mutableStateOf<ConsultationResponse?>(null) }


    LaunchedEffect(patientId) {
        if (patientId != null) {
            consultationViewModel.loadPatient(patientId)
            consultationViewModel.loadPatientConsultations(patientId)
        }
    }

    // Observar o estado de exclusão de consulta para exibir Toast e recarregar
    LaunchedEffect(deleteConsultationState) { // --- USAR deleteConsultationState ---
        when (deleteConsultationState) {
            is DeleteConsultationUiState.Success -> { // --- USAR DeleteConsultationUiState ---
                Toast.makeText(context, "Consulta excluída com sucesso!", Toast.LENGTH_SHORT).show()
                consultationViewModel.resetDeleteConsultationState() // --- CHAMADA CORRETA ---
                // Recarrega as consultas do paciente para atualizar a lista no prontuário
                patientId?.let { consultationViewModel.loadPatientConsultations(it) }
            }
            is DeleteConsultationUiState.Error -> { // --- USAR DeleteConsultationUiState ---
                val errorMessage = (deleteConsultationState as DeleteConsultationUiState.Error).message // --- USAR DeleteConsultationUiState ---
                Toast.makeText(context, "Erro ao excluir consulta: $errorMessage", Toast.LENGTH_LONG).show()
                consultationViewModel.resetDeleteConsultationState() // --- CHAMADA CORRETA ---
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
                .padding(horizontal = 16.dp)
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

                    patient.date_of_birth?.let { apiDateString ->
                        val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val apiFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                        val formattedDate = try {
                            apiFormat.parse(apiDateString)?.let { dateObject ->
                                displayFormat.format(dateObject)
                            }
                        } catch (e: ParseException) { null } catch (e: Exception) { null }

                        if (formattedDate != null) {
                            Text(
                                text = "Data de Nascimento: $formattedDate",
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


                    when (patientConsultationsUiState) {
                        is PatientConsultationsUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            Text("Carregando histórico de consultas...", modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                        is PatientConsultationsUiState.Loaded -> {
                            val consultations = (patientConsultationsUiState as PatientConsultationsUiState.Loaded).consultations

                            if (consultations.isEmpty()) {
                                Text(text = "Nenhuma consulta registrada para este paciente.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                val sortedConsultations = consultations.sortedByDescending {
                                    try {
                                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault()).parse(it.dateConsultation) ?: Date(0)
                                    } catch (e: Exception) {
                                        Date(0)
                                    }
                                }

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
                                                        modifier = Modifier.weight(1f) // Permite que o texto ocupe espaço
                                                    )
                                                    // --- ÍCONES DE AÇÃO ---
                                                    Row(horizontalArrangement = Arrangement.End) {
                                                        IconButton(
                                                            onClick = {
                                                                // Lógica de Edição da Consulta
                                                                consultation.id?.let { consId ->
                                                                    // Navega para a tela de consulta para edição
                                                                    // Você pode adaptar ConsultationScreen para edição ou criar uma nova tela EditConsultationScreen
                                                                    // Exemplo: navController.navigate("edit_consultation_screen/${consId}")
                                                                    Toast.makeText(context, "Editar consulta ${consId}", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                        ) {
                                                            Icon(Icons.Default.Edit, contentDescription = "Editar Consulta")
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                // Lógica de Exclusão da Consulta
                                                                consultationToDelete = consultation // Armazena a consulta para exclusão
                                                                showDeleteConfirmationDialog = true // Mostra o diálogo
                                                            }
                                                        ) {
                                                            Icon(Icons.Default.Delete, contentDescription = "Excluir Consulta")
                                                        }
                                                    }
                                                    // --- FIM ÍCONES DE AÇÃO ---
                                                } // Fim da Row para data e ícones

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
                                                    if (photoLoc.startsWith("http://") || photoLoc.startsWith("https://")) {
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Image(
                                                            painter = rememberImagePainter(data = photoLoc),
                                                            contentDescription = "Foto da Consulta",
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(200.dp),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
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
                            Text("Aguardando histórico de consultas...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

    // --- DIÁLOGO DE CONFIRMAÇÃO DE EXCLUSÃO DE CONSULTA ---
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
                            consultationViewModel.deleteConsultation(consId) // Chama o ViewModel para excluir a consulta
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
    // --- FIM DIÁLOGO ---
}