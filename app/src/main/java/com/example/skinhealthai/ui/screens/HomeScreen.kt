package com.example.skinhealthai.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.skinhealthai.data.model.Patient
import com.example.skinhealthai.data.samplePatients
import com.example.skinhealthai.ui.components.FeatureCard
import com.example.skinhealthai.ui.components.TopBarLoggedIn
import com.example.skinhealthai.ui.components.modals.PatientListModal
import com.example.skinhealthai.ui.components.modals.PatientRegisterModal
import com.example.skinhealthai.ui.screens.CameraCapture
import com.example.skinhealthai.utils.FileUtils
import com.example.skinhealthai.viewmodel.ImageUploadViewModel
import com.example.skinhealthai.viewmodel.LoginViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    loginViewModel: LoginViewModel = viewModel(),
) {

    val userName by loginViewModel.loggedInUserName.collectAsState()

    Scaffold(
        topBar = {
            TopBarLoggedIn(
                userName = userName ?: "Usuário",
                onLogout = {
                    loginViewModel.logout()
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
            item { WelcomeHeader(userName ?: "Usuário") }

            item {
                FeaturesSection(
                    onNovaAnaliseClick = { navController.navigate("patient_list") },
                    onCadastrarPacienteClick = { navController.navigate("patient_register") },
                    onHistoricoClick = { navController.navigate("analysis_history") }
                )
            }

            item { RecentPatientsTitle() }

            item {
                RecentPatientsTable(
                    patients = samplePatients.take(3),
                    onPatientClick = { patientId ->
                        patientId?.let { id ->
                            navController.navigate("patient_record/${id}")
                        } ?: run {
                            println("ID do paciente nulo, não é possível navegar.")
                        }
                    }
                )
            }

            item {
                TextButton(
                    onClick = { navController.navigate("patient_list") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver Todos os Pacientes", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Ver todos")
                    }
                }
            }

            item { Footer() }
        }
    }

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
    onCadastrarPacienteClick: () -> Unit,
    onHistoricoClick: () -> Unit
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
            onClick = onHistoricoClick
        )
        FeatureCard(
            title = "Cadastrar novo Paciente",
            description = "Cadastre um novo paciente no sistema",
            onClick = onCadastrarPacienteClick
        )
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
    onPatientClick: (Int?) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("PACIENTE", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }

            HorizontalDivider()

            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                if (patients.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhum paciente recente encontrado.",
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(patients) { patient ->
                        PatientRow(patient = patient, onClick = { onPatientClick(patient.id) })
                        HorizontalDivider()
                    }
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
        Text(patient.name, modifier = Modifier.weight(1f))
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