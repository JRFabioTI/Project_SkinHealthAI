package com.example.skinhealthai.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

import com.example.skinhealthai.data.sampleAnalysisHistory
import com.example.skinhealthai.data.model.AnalysisHistoryItem
import com.example.skinhealthai.ui.theme.screens.RiskBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisHistoryScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Análises", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (sampleAnalysisHistory.isEmpty()) {
                item {
                    Text(
                        text = "Nenhum histórico de análise encontrado.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            } else {
                items(sampleAnalysisHistory) { item ->
                    AnalysisHistoryListItem(
                        analysisItem = item,
                        onClick = { analysisId ->
                            println("Clicado na análise com ID: $analysisId")
                        }
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
fun AnalysisHistoryListItem(
    analysisItem: AnalysisHistoryItem,
    onClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(analysisItem.id) }
            .padding(vertical = 12.dp, horizontal = 0.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Paciente: ${analysisItem.patientName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Data: ${analysisItem.date}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Diagnóstico: ${analysisItem.diagnosis}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        RiskBadge(
            risk = analysisItem.risk,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}