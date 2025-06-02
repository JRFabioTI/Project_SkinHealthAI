package com.example.skinhealthai.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.Alignment
// ... (seus imports)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRegisterScreen(
    navController: NavHostController,
    patientId: Int? = null,
    viewModel: PatientViewModel = viewModel()
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    var selectedGender by remember { mutableStateOf<String?>(null) } // Guarda o texto completo (Masculino, Feminino, Outro)
    var phone by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var birthDateError by remember { mutableStateOf(false) }
    var genderError by remember { mutableStateOf(false) }
    var cpfError by remember { mutableStateOf(false) }

    val genderOptions = listOf("Masculino", "Feminino", "Outro") // Opções de exibição no dropdown
    val registerState by viewModel.registerState.collectAsState()
    val selectedPatientState by viewModel.selectedPatient.collectAsState()
    val isEditing = patientId != null

    LaunchedEffect(patientId) {
        // Resetar estados e campos ao carregar um novo paciente/entrar no modo de criação
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
        viewModel.resetSelectedPatientState() // Limpa o estado do paciente selecionado no ViewModel

        if (isEditing && patientId != null) {
            viewModel.fetchPatientById(patientId) // Busca os dados do paciente para edição
        }
    }

    LaunchedEffect(selectedPatientState) {
        when (selectedPatientState) {
            is SinglePatientUiState.Success -> {
                val patient = (selectedPatientState as SinglePatientUiState.Success).patient
                name = patient.name
                patient.date_of_birth?.let { apiDateString ->
                    val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    displayFormat.isLenient = false

                    try {
                        displayFormat.parse(apiDateString)
                        birthDate = apiDateString // Se a API já retorna dd/MM/yyyy
                    } catch (e: ParseException) {
                        try {
                            val apiFormatAlt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            apiFormatAlt.isLenient = false
                            val dateObject = apiFormatAlt.parse(apiDateString)
                            birthDate = dateObject?.let { displayFormat.format(it) } ?: ""
                        } catch (e2: ParseException) {
                            birthDate = ""
                            Toast.makeText(context, "Erro: Data da API em formato desconhecido: $apiDateString", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        birthDate = ""
                        Toast.makeText(context, "Erro inesperado ao processar data da API: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                // --- CORREÇÃO AQUI para pré-selecionar o GÊNERO ---
                // A API retorna "Masculino", "Feminino", "Outro".
                // Mapeie esse texto para a opção do seu dropdown.
                selectedGender = when (patient.gender) {
                    "Masculino" -> "Masculino"
                    "Feminino" -> "Feminino"
                    "Outro" -> "Outro"
                    // Adicione aqui se a API puder retornar as siglas "M", "F", "O" na leitura
                    "M" -> "Masculino"
                    "F" -> "Feminino"
                    "O" -> "Outro"
                    else -> null // Se for algo diferente, não seleciona nada
                }
                // --- FIM CORREÇÃO GÊNERO ---

                phone = patient.cellphone ?: ""
                cpf = patient.cpf ?: ""
                email = patient.email ?: ""
            }
            is SinglePatientUiState.Error -> {
                val errorMessage = (selectedPatientState as SinglePatientUiState.Error).message
                Toast.makeText(context, "Erro ao carregar paciente: $errorMessage", Toast.LENGTH_LONG).show()
                navController.popBackStack() // Volta se não conseguir carregar o paciente
            }
            is SinglePatientUiState.Loading -> { /* Opcional: Mostrar um indicador de carregamento */ }
            is SinglePatientUiState.Idle -> { /* Nada a fazer, estado inicial */ }
        }
    }

    LaunchedEffect(registerState) {
        when (registerState) {
            is PatientUiState.Success -> {
                val message = if (isEditing) "Paciente atualizado com sucesso!" else "Paciente cadastrado com sucesso!"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                navController.popBackStack()
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
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                    value = selectedGender ?: "", // Usa selectedGender aqui
                    onValueChange = {},
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
                    genderOptions.forEach { genderText -> // genderText é o texto completo do dropdown
                        DropdownMenuItem(
                            text = { Text(genderText) },
                            onClick = {
                                selectedGender = genderText // selectedGender agora é o texto completo
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

            Spacer(modifier = Modifier.height(5.dp))

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

            Spacer(modifier = Modifier.height(27.dp))

            Button(
                onClick = {
                    nameError = name.isEmpty()
                    birthDateError = birthDate.isNotEmpty() && !isValidDate(birthDate)
                    genderError = selectedGender == null
                    cpfError = cpf.isNotEmpty() && cpf.length != 11

                    if (nameError || birthDateError || genderError || cpfError) {
                        Toast.makeText(context, "Por favor, corrija os erros no formulário.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    // --- CORREÇÃO AQUI: Mapear o TEXTO COMPLETO para a SIGLA para a API ---
                    val genderCodeForApi = when (selectedGender) {
                        "Masculino" -> "M"
                        "Feminino" -> "F"
                        "Outro" -> "O"
                        else -> null // Deve ser tratado como erro de validação se for obrigatório
                    }
                    // --- FIM CORREÇÃO ---

                    val formattedDateForApi = try {
                        if (birthDate.isNotEmpty()) {
                            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            inputFormat.isLenient = false

                            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            val dateObject = inputFormat.parse(birthDate)
                            outputFormat.format(dateObject)
                        } else {
                            null
                        }
                    } catch (e: ParseException) {
                        Toast.makeText(context, "Erro de formatação de data ao salvar. Verifique o formato DD/MM/AAAA.", Toast.LENGTH_LONG).show()
                        birthDateError = true
                        return@Button
                    } catch (e: Exception) {
                        Toast.makeText(context, "Erro inesperado na data ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    val patientRequest = PatientRequest(
                        name = name,
                        date_of_birth = formattedDateForApi,
                        gender = genderCodeForApi, // ENVIANDO A SIGLA PARA A API
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
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .align(Alignment.CenterHorizontally),
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

private fun isValidDate(dateString: String): Boolean {
    if (dateString.isEmpty()) return true

    return try {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        format.isLenient = false
        format.parse(dateString)
        true
    } catch (e: ParseException) {
        false
    }
}