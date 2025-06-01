package com.example.skinhealthai.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.* // Importe tudo de runtime, incluindo mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.skinhealthai.data.model.PatientRequest
import com.example.skinhealthai.ui.viewmodel.PatientUiState
import com.example.skinhealthai.ui.viewmodel.PatientViewModel
import com.example.skinhealthai.ui.viewmodel.SinglePatientUiState
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRegisterScreen(
    navController: NavHostController,
    patientId: Int? = null,
    viewModel: PatientViewModel = viewModel()
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") } // Data no formato de exibição (dd/MM/yyyy)
    var genderExpanded by remember { mutableStateOf(false) }
    var selectedGender by remember { mutableStateOf<String?>(null) } // Guarda a string de exibição ("Masculino")
    var phone by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var birthDateError by remember { mutableStateOf(false) } // CORRIGIDO: mutableStateOf
    var genderError by remember { mutableStateOf(false) }
    var cpfError by remember { mutableStateOf(false) }

    val genderOptions = listOf("Masculino", "Feminino", "Outro") // Opções para o dropdown

    val registerState by viewModel.registerState.collectAsState()
    val selectedPatientState by viewModel.selectedPatient.collectAsState()

    val isEditing = patientId != null

    // LaunchedEffect para carregar os dados do paciente se estiver em modo de edição
    LaunchedEffect(patientId) {
        // Limpar campos e erros sempre que o patientId mudar (novo ou diferente paciente)
        name = ""
        birthDate = ""
        selectedGender = null
        phone = ""
        cpf = ""
        email = ""
        nameError = false
        birthDateError = false
        genderError = false
        cpfError = false
        viewModel.resetSelectedPatientState() // Resetar estado do ViewModel também

        if (isEditing && patientId != null) {
            viewModel.fetchPatientById(patientId)
        }
    }

    // LaunchedEffect para preencher os campos quando os dados do paciente forem carregados
    LaunchedEffect(selectedPatientState) {
        when (selectedPatientState) {
            is SinglePatientUiState.Success -> {
                val patient = (selectedPatientState as SinglePatientUiState.Success).patient
                name = patient.name

                // CORREÇÃO DA DATA DE NASCIMENTO PARA EXIBIÇÃO USANDO SimpleDateFormat:
                // API retorna dd/MM/yyyy. Queremos DD/MM/YYYY para o usuário.
                patient.date_of_birth?.let { apiDateString ->
                    val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    displayFormat.isLenient = false

                    try {
                        // Tenta ver se já é o formato de exibição DD/MM/YYYY
                        displayFormat.parse(apiDateString) // Isso validará se está no formato
                        birthDate = apiDateString // Se parsear, já está no formato certo
                    } catch (e: ParseException) {
                        // Se não parsear como DD/MM/YYYY, pode ser YYYY-MM-DD ou outro (menos provável, mas tratado)
                        try {
                            val apiFormatAlt = SimpleDateFormat("yyyy-MM-dd", Locale.US) // Para o caso de ser YYYY-MM-DD
                            apiFormatAlt.isLenient = false
                            val dateObject = apiFormatAlt.parse(apiDateString)
                            birthDate = dateObject?.let { displayFormat.format(it) } ?: ""
                        } catch (e2: ParseException) {
                            birthDate = "" // Falha total na conversão
                            Toast.makeText(context, "Erro: Data da API em formato desconhecido: $apiDateString", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        birthDate = "" // Captura outras exceções inesperadas
                        Toast.makeText(context, "Erro inesperado ao processar data da API: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                // CORREÇÃO DO GÊNERO: Mapeie o código do gênero para o texto de exibição
                selectedGender = when (patient.gender?.uppercase(Locale.getDefault())) {
                    "M" -> "Masculino"
                    "F" -> "Feminino"
                    "O" -> "Outro"
                    else -> null // Se for algo inesperado, não seleciona nada
                }
                phone = patient.cellphone ?: ""
                cpf = patient.cpf ?: ""
                email = patient.email ?: ""
            }
            is SinglePatientUiState.Error -> {
                val errorMessage = (selectedPatientState as SinglePatientUiState.Error).message
                Toast.makeText(context, "Erro ao carregar paciente: $errorMessage", Toast.LENGTH_LONG).show()
            }
            is SinglePatientUiState.Loading -> { /* Opcional: Mostrar um indicador de carregamento */ }
            is SinglePatientUiState.Idle -> { /* Nada a fazer, estado inicial */ }
        }
    }

    // Reage a mudanças no estado de registro/atualização
    LaunchedEffect(registerState) {
        when (registerState) {
            is PatientUiState.Success -> {
                val message = if (isEditing) "Paciente atualizado com sucesso!" else "Paciente cadastrado com sucesso!"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                navController.popBackStack() // Volta para a tela anterior
                viewModel.resetRegisterState()
            }
            is PatientUiState.Error -> {
                Toast.makeText(context, (registerState as PatientUiState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetRegisterState()
            }
            else -> { /* idle ou loading, nada a fazer aqui */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Paciente" else "Cadastrar Novo Paciente") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = it.isEmpty()
                },
                label = { Text("Nome") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = nameError,
                supportingText = { if (nameError) Text("O nome é obrigatório") }
            )

            OutlinedTextField(
                value = birthDate,
                onValueChange = {
                    birthDate = it
                    birthDateError = it.isNotEmpty() && !isValidDate(it)
                },
                label = { Text("Data de Nascimento (dd/mm/aaaa)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                isError = birthDateError,
                supportingText = { if (birthDateError) Text("Formato inválido (dd/mm/aaaa)") }
            )

            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded }
            ) {
                OutlinedTextField(
                    value = selectedGender ?: "", // Exibe o valor selecionado
                    onValueChange = {}, // readOnly é true, então não altera o valor aqui
                    readOnly = true,
                    label = { Text("Gênero") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    isError = genderError,
                    supportingText = { if (genderError) Text("Selecione um gênero") }
                )
                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    genderOptions.forEach { gender ->
                        DropdownMenuItem(
                            text = { Text(gender) },
                            onClick = {
                                selectedGender = gender
                                genderExpanded = false
                                genderError = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Telefone") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cpf,
                onValueChange = {
                    cpf = it
                    cpfError = it.isNotEmpty() && it.length != 11
                },
                label = { Text("CPF") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = cpfError,
                supportingText = { if (cpfError) Text("CPF deve ter 11 dígitos") }
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    // Validações antes de enviar
                    nameError = name.isEmpty()
                    birthDateError = birthDate.isNotEmpty() && !isValidDate(birthDate)
                    genderError = selectedGender == null
                    cpfError = cpf.isNotEmpty() && cpf.length != 11

                    if (nameError || birthDateError || genderError || cpfError) {
                        Toast.makeText(context, "Por favor, corrija os erros no formulário.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    // Mapeia a string de exibição do gênero ("Masculino") para o código da API ('M')
                    val genderCode = when (selectedGender?.lowercase(Locale.getDefault())) {
                        "masculino" -> "M"
                        "feminino" -> "F"
                        "outro" -> "O"
                        else -> null
                    }

                    // Formata a data de entrada (DD/MM/YYYY) para o formato da API (YYYY-MM-DD)
                    val formattedDateForApi = try {
                        if (birthDate.isNotEmpty()) {
                            // Formato de entrada (usuário): DD/MM/YYYY
                            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            inputFormat.isLenient = false // Garante validação estrita para entrada do usuário

                            // Formato de saída (API): YYYY-MM-DD
                            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US) // Use Locale.US para garantir ISO 8601

                            // 1. Parse a string de entrada para um objeto Date
                            val dateObject = inputFormat.parse(birthDate)
                            // 2. Formate o objeto Date para a string de saída
                            outputFormat.format(dateObject)
                        } else {
                            null
                        }
                    } catch (e: ParseException) {
                        Toast.makeText(context, "Erro de formatação de data ao salvar. Verifique o formato DD/MM/AAAA.", Toast.LENGTH_LONG).show()
                        birthDateError = true // Marca o erro
                        return@Button // Impede o envio
                    } catch (e: Exception) {
                        Toast.makeText(context, "Erro inesperado na data ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
                        return@Button // Impede o envio
                    }

                    val patientRequest = PatientRequest(
                        name = name,
                        date_of_birth = formattedDateForApi,
                        gender = genderCode,
                        cellphone = phone.takeIf { it.isNotBlank() },
                        cpf = cpf.takeIf { it.isNotBlank() },
                        email = email.takeIf { it.isNotBlank() }
                    )

                    if (isEditing && patientId != null) {
                        viewModel.updatePatient(patientId, patientRequest)
                    } else {
                        viewModel.registerPatient(patientRequest)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = registerState !is PatientUiState.Loading && selectedPatientState !is SinglePatientUiState.Loading
            ) {
                if (registerState is PatientUiState.Loading || selectedPatientState is SinglePatientUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (isEditing) "Atualizar" else "Salvar")
                }
            }
        }
    }
}


// Função auxiliar para validação de data (para o formato DD/MM/YYYY do usuário)
private fun isValidDate(dateString: String): Boolean {
    if (dateString.isEmpty()) return true

    return try {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        format.isLenient = false // Impede que "31/02/2025" seja considerado válido
        format.parse(dateString)
        true
    } catch (e: ParseException) {
        false
    }
}