package com.example.skinhealthai.ui.theme.screens

import PatientListModal
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skinhealthai.ui.screens.CameraCapture
import com.example.skinhealthai.utils.FileUtils
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.navigation.NavHostController
import com.example.skinhealthai.viewmodel.ImageUploadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    imageViewModel: ImageUploadViewModel,
) {
    var showCamera by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showPatientListModal by remember { mutableStateOf(false) }
    var showPatientRegisterModal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBarLoggedIn(
                userName = "Dr. Médico",
                onLogout = {
                    // TODO: implementar logout real
                    println("Usuário deslogou")
                    // Pode navegar para login aqui se quiser
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { WelcomeHeader("Dr(a). Médico") }

            item {
                FeaturesSection(
                    onNovaAnaliseClick = { showCamera = true },
                    onCadastrarPacienteClick = { showPatientRegisterModal = true }
                )
            }

            item { RecentPatientsTitle() }

            item {
                RecentPatientsTable(
                    patients = samplePatients,
                    onPatientClick = { patientId ->
                        // Navegar para detalhes do paciente, exemplo:
                        navController.navigate("patient_record")
                    }
                )
            }

            item { Footer() }
        }
    }

    if (showCamera) {
        CameraCapture(
            onImageCaptured = { bitmap ->
                showCamera = false
                bitmap?.let {
                    FileUtils.saveBitmapToGallery(context, it)
                    imageViewModel.capturedBitmap = it
                }
                showPatientListModal = true
            }
        )
    }

    if (showPatientListModal) {
        PatientListModal(
            onDismissRequest = { showPatientListModal = false },
            onNewPatientClick = {
                showPatientListModal = false
                showPatientRegisterModal = true
            },
            navController = navController,
            imageViewModel = imageViewModel
        )
    }

    if (showPatientRegisterModal) {
        PatientRegisterModal(
            onDismissRequest = { showPatientRegisterModal = false },
            onSave = {
                showPatientRegisterModal = false
                navController.navigate("patient_record")
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarLoggedIn(
    userName: String,
    onLogout: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = "SkinHealthAI",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            IconButton(onClick = { /* TODO: notificações */ }) {
                Icon(Icons.Default.Notifications, contentDescription = "Notificações")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = "Usuário", modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = userName)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Logout") },
                    onClick = {
                        expanded = false
                        onLogout()
                    }
                )
            }
        }
    )
}

@Composable
fun WelcomeHeader(name: String) {
    Column {
        Text(
            text = "Bem-vindo(a), $name",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Use o SkinHealthAI para análise assistida de lesões cutâneas com IA.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun FeaturesSection(
    onNovaAnaliseClick: () -> Unit,
    onCadastrarPacienteClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FeatureCard(
            title = "Nova Análise",
            description = "Capture ou envie imagem para diagnóstico IA",
            onClick = onNovaAnaliseClick
        )
        FeatureCard(
            title = "Histórico",
            description = "Veja exames anteriores do mesmo paciente",
            onClick = { /* TODO: Implementar histórico */ }
        )
        FeatureCard(
            title = "Cadastrar novo Paciente",
            description = "Cadastre um novo paciente no sistema",
            onClick = onCadastrarPacienteClick
        )
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun RecentPatientsTitle() {
    Text(
        text = "Pacientes Recentes",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun RecentPatientsTable(
    patients: List<Patient>,
    onPatientClick: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("PACIENTE", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                Text("IDADE", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("ÚLTIMA ANÁLISE", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                Text("RESULTADO", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                Text("AÇÕES", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }

            HorizontalDivider()

            LazyColumn {
                items(patients) { patient ->
                    PatientRow(patient = patient, onClick = { onPatientClick(patient.id) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun PatientRow(patient: Patient, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(patient.name, modifier = Modifier.weight(2f))
        Text(patient.age.toString(), modifier = Modifier.weight(1f))
        Text(patient.lastAnalysis, modifier = Modifier.weight(2f))
        RiskBadge(patient.risk, modifier = Modifier.weight(2f))
        TextButton(onClick = onClick, modifier = Modifier.weight(1f)) {
            Text("Ver")
        }
    }
}

@Composable
fun RiskBadge(risk: String, modifier: Modifier = Modifier) {
    val color = when (risk) {
        "Baixo Risco" -> Color(0xFF4CAF50)
        "Médio Risco" -> Color(0xFFFFC107)
        "Alto Risco" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small,
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = risk,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 14.sp
        )
    }
}

@Composable
fun Footer() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "© 2024 SkinHealthAI - Todos os direitos reservados",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// Dados de exemplo
val samplePatients = listOf(
    Patient("1", "João Silva", 38, "10/05/2024", "Baixo Risco"),
    Patient("2", "Maria Souza", 29, "12/05/2024", "Médio Risco"),
    Patient("3", "Ana Oliveira", 46, "15/05/2024", "Alto Risco"),
)

data class Patient(
    val id: String,
    val name: String,
    val age: Int,
    val lastAnalysis: String,
    val risk: String
)
