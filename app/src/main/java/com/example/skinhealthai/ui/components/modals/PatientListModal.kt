package com.example.skinhealthai.ui.components.modals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.example.skinhealthai.data.model.Patient
import com.example.skinhealthai.data.samplePatients
import com.example.skinhealthai.viewmodel.ImageUploadViewModel

@Composable
fun PatientListModal(
    onDismissRequest: () -> Unit,
    onNewPatientClick: () -> Unit,
    navController: NavHostController,
    imageViewModel: ImageUploadViewModel
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxSize(0.6f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Selecione o paciente ou cadastre um novo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                val patients = samplePatients

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp)
                ) {
                    if (patients.isEmpty()) {
                        item {
                            Text(
                                text = "Nenhum paciente cadastrado para selecionar.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(patients) { patient ->
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {

                                            patient.id?.let { id ->
                                                navController.navigate("patient_record/${id}")
                                            }

                                            onDismissRequest()
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = patient.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Divider()
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onDismissRequest()
                        onNewPatientClick()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cadastrar Novo Paciente")
                }
            }
        }
    }
}