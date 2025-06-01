package com.example.skinhealthai.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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