import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.example.skinhealthai.ui.theme.screens.Patient
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
        properties = DialogProperties(usePlatformDefaultWidth = false) // dialog full width
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
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                val patients = listOf(
                    Patient("JS", "João Silva", 45, "15/06/2023", "Baixo Risco"),
                    Patient("MA", "Maria Almeida", 62, "10/06/2023", "Médio Risco"),
                    Patient("CR", "Carlos Roberto", 54, "12/05/2023", "Alto Risco"),
                    Patient("LA", "Larissa Alves", 29, "01/04/2023", "Baixo Risco"),
                    Patient("FG", "Fernando Gomes", 37, "22/03/2023", "Médio Risco"),
                    Patient("PS", "Patrícia Silva", 43, "15/02/2023", "Alto Risco")
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp)
                ) {
                    items(patients) { patient ->
                        Text(
                            text = patient.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // ação de escolher paciente
                                    onDismissRequest()
                                    // navegar ou salvar dados
                                }
                                .padding(12.dp)
                        )
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
