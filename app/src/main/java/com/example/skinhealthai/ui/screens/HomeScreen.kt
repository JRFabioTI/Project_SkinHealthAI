package com.example.skinhealthai.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.skinhealthai.ui.components.FeatureCard
import com.example.skinhealthai.ui.components.TopBarLoggedIn
import com.example.skinhealthai.viewmodel.LoginViewModel
import com.example.skinhealthai.ui.screens.AppRoutes

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
                    onCadastrarPacienteClick = { navController.navigate(AppRoutes.PATIENT_REGISTER) },
                    onNovaConsultaClick = { navController.navigate(AppRoutes.PATIENT_LIST) },
                    onHistoricoClick = { navController.navigate(AppRoutes.PATIENT_HISTORY) }
                )
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
    onNovaConsultaClick: () -> Unit,
    onCadastrarPacienteClick: () -> Unit,
    onHistoricoClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FeatureCard(
            title = "Cadastrar Paciente",
            description = "Cadastre um novo paciente no sistema",
            onClick = onCadastrarPacienteClick
        )
        FeatureCard(
            title = "Nova Consulta",
            description = "Crie uma consulta para para seu paciente e capture/envie imagem e receba o diagnóstico IA",
            onClick = onNovaConsultaClick
        )
        FeatureCard(
            title = "Histórico de Prontuários",
            description = "Veja o histórico de todos os prontuários de seus pacientes",
            onClick = onHistoricoClick
        )
    }
}


@Composable
fun Footer() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))
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