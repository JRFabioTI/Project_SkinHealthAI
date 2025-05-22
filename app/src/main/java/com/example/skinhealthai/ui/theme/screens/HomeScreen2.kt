package com.example.skinhealthai.ui.theme.screens

import PatientListModal
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skinhealthai.ui.screens.CameraCapture
import android.graphics.Bitmap
import androidx.compose.runtime.*
import com.example.skinhealthai.utils.FileUtils
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.navigation.NavHostController
import com.example.skinhealthai.viewmodel.ImageUploadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen2(
    navController: NavHostController,
    imageViewModel: ImageUploadViewModel,
) {
    var showCamera by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showPatientListModal by remember { mutableStateOf(false) }
    var showPatientRegisterModal by remember { mutableStateOf(false) }
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }


    Scaffold(
        topBar = {
            TopBarLoggedIn(
                userName = "Dr. Médico",
                onLogout = {
                    println("Usuário deslogou")
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                WelcomeHeader("Dr(a). Médico")
            }

            item {
                FeaturesSection(
                    onNovaAnaliseClick = { showPatientListModal = true },
                    onCadastrarPacienteClick = { showPatientRegisterModal = true }
                )
            }

            item {
                RecentPatientsTitle()
            }

            item {
                RecentPatientsTable()
            }

            item {
                Footer()
            }
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
                selectedPatient?.let { patient ->
                    navController.navigate("patient_record/${patient.id}")
                }
            }
        )
    }

    // Modal lista de pacientes
    if (showPatientListModal) {
        PatientListModal(
            onDismissRequest = { showPatientListModal = false },
            onPatientSelected = { patient ->
                selectedPatient = patient
                showPatientListModal = false
                showCamera = true
            },
            onNewPatientClick = {
                showPatientListModal = false
                showPatientRegisterModal = true
            },
            navController = navController,
            imageViewModel = imageViewModel
        )
    }

// Modal cadastro paciente simplificado
    if (showPatientRegisterModal) {
        PatientRegisterModal(
            onDismissRequest = { showPatientRegisterModal = false },
            onSave = {
                showPatientRegisterModal = false
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
            IconButton(onClick = { /* Ação para notificações */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notificações"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Usuário",
                    modifier = Modifier.size(32.dp)
                )
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
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FeatureCard(
            title = "Nova Análise",
            description = "Capture ou envie imagem para diagnóstico IA",
            onClick = onNovaAnaliseClick
        )

        FeatureCard(
            title = "Histórico",
            description = "Veja exames anteriores do mesmo paciente",
            onClick = { }
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
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
fun RecentPatientsTable() {
    val patients = listOf(
        Patient("JS", "João Silva", 45, "15/06/2023", "Baixo Risco"),
        Patient("MA", "Maria Almeida", 62, "10/06/2023", "Médio Risco")
    )

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("PACIENTE", fontWeight = FontWeight.Bold)
                Text("IDADE", fontWeight = FontWeight.Bold)
                Text("ÚLTIMA ANÁLISE", fontWeight = FontWeight.Bold)
                Text("RESULTADO", fontWeight = FontWeight.Bold)
                Text("AÇÕES", fontWeight = FontWeight.Bold)
            }

            Divider()

            patients.forEach { patient ->
                PatientRow(patient)
                Divider()
            }
        }
    }
}

@Composable
fun PatientRow(patient: Patient) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(patient.id, modifier = Modifier.weight(0.8f))
        Text(patient.name, modifier = Modifier.weight(2f))
        Text(patient.age.toString(), modifier = Modifier.weight(0.8f))
        Text(patient.lastAnalysis, modifier = Modifier.weight(1.2f))

        Box(modifier = Modifier.weight(1f)) {
            RiskBadge(patient.risk)
        }

        TextButton(
            onClick = { /* Action */ },
            modifier = Modifier.weight(0.8f)
        ) {
            Text("Ver")
        }
    }
}

@Composable
fun RiskBadge(risk: String) {
    val color = when (risk) {
        "Baixo Risco" -> Color.Green
        "Médio Risco" -> Color.Yellow
        "Alto Risco" -> Color.Red
        else -> Color.Gray
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = risk,
            color = color,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun Footer() {
    Text(
        text = "Corporar",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )
}

data class Patient(
    val id: String,
    val name: String,
    val age: Int,
    val lastAnalysis: String,
    val risk: String
)

