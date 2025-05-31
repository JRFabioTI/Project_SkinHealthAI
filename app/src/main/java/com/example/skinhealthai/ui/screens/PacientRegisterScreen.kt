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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException // Importe para capturar exceções de data
import java.text.ParseException // Importe para capturar exceções de parse
import java.text.SimpleDateFormat
import java.util.Locale // Importe Locale para formatação

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRegisterScreen(
    navController: NavHostController,
    // Removido 'userToken: String' - o token será tratado automaticamente pelo RetrofitInstance
    viewModel: PatientViewModel = viewModel()
) {
    val context = LocalContext.current
    // Removido coroutineScope, pois a chamada ao ViewModel já está em um ViewModelScope

    var name by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    var selectedGender by remember { mutableStateOf<String?>(null) }
    var phone by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Estados para validação
    var nameError by remember { mutableStateOf(false) }
    var birthDateError by remember { mutableStateOf(false) }
    var genderError by remember { mutableStateOf(false) }
    var cpfError by remember { mutableStateOf(false) }

    val genderOptions = listOf("Masculino", "Feminino", "Outro")

    val registerState by viewModel.registerState.collectAsState()

    // Reage a mudanças no estado para sucesso e erro
    LaunchedEffect(registerState) {
        when (registerState) {
            is PatientUiState.Success -> {
                Toast.makeText(context, "Paciente cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                navController.popBackStack() // Volta para a tela anterior
                viewModel.resetRegisterState()
            }
            is PatientUiState.Error -> {
                Toast.makeText(context, (registerState as PatientUiState.Error).message, Toast.LENGTH_LONG).show() // Use LONG para erros
                viewModel.resetRegisterState()
            }
            else -> { /* idle ou loading, nada a fazer aqui */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cadastrar Novo Paciente") },
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
                    nameError = it.isEmpty() // Atualiza o erro em tempo real
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
                    birthDateError = !isValidDate(it) && it.isNotEmpty() // Valida ao digitar
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
                    value = selectedGender ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gênero") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), // Adicione .menuAnchor() aqui
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
                                genderError = false // Limpa o erro ao selecionar
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
                    cpfError = it.isNotEmpty() && it.length != 11 // Valida CPF (apenas tamanho por enquanto)
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
                    // Validações finais antes de enviar
                    nameError = name.isEmpty()
                    birthDateError = !isValidDate(birthDate) && birthDate.isNotEmpty()
                    genderError = selectedGender == null
                    cpfError = cpf.isNotEmpty() && cpf.length != 11

                    // Se houver algum erro, não prossegue
                    if (nameError || birthDateError || genderError || cpfError) {
                        Toast.makeText(context, "Por favor, corrija os erros no formulário.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    // Mapeia o gênero e a data para o formato da API
                    val genderCode = when (selectedGender?.lowercase()) {
                        "masculino" -> "M"
                        "feminino" -> "F"
                        "outro" -> "O"
                        else -> null
                    }

                    val formattedDate = try {
                        if (birthDate.isNotEmpty()) {
                            // Parser para o formato de entrada (do usuário: dd/MM/yyyy)
                            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            inputFormat.isLenient = false // Importante: Garante que datas inválidas como "31/02" falhem

                            // Formatter para o formato de saída (para a API: yyyy-MM-dd)
                            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                            // 1. Parse a string de entrada para um objeto Date
                            val dateObject = inputFormat.parse(birthDate)

                            // 2. Formate o objeto Date para a string de saída
                            outputFormat.format(dateObject)
                        } else {
                            null // Se o campo estiver vazio, retorna null
                        }
                    } catch (e: ParseException) {
                        // Captura a exceção se a data digitada for inválida
                        null // Se a data for inválida, envia null para a API ou trate como erro
                    } catch (e: Exception) {
                        // Captura outras exceções inesperadas
                        null
                    }

                    val patientRequest = PatientRequest(
                        name = name,
                        date_of_birth = formattedDate,
                        gender = genderCode,
                        cellphone = phone.takeIf { it.isNotBlank() },
                        cpf = cpf.takeIf { it.isNotBlank() },
                        email = email.takeIf { it.isNotBlank() }
                    )

                    // Chama o ViewModel para registrar o paciente
                    viewModel.registerPatient(patientRequest)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = registerState !is PatientUiState.Loading // Desabilita o botão enquanto carrega
            ) {
                if (registerState is PatientUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Salvar")
                }
            }
        }
    }
}


// Função auxiliar para validação de data
private fun isValidDate(dateString: String): Boolean {
    if (dateString.isEmpty()) return true // Campo opcional pode ser vazio

    return try {
        // Cria um SimpleDateFormat com o padrão esperado pelo usuário
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        // Importante: Define como não-tolerante para que datas inválidas
        // (como "31/02/2025") lancem ParseException
        format.isLenient = false
        // Tenta fazer o parse da string de data
        format.parse(dateString)
        // Se o parse for bem-sucedido, a data é válida
        true
    } catch (e: ParseException) {
        // Se ocorrer uma ParseException, a data não está no formato ou é inválida
        false
    }
}