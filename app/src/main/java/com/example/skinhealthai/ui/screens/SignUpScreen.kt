package com.example.skinhealthai.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.skinhealthai.data.model.UserRequest
import com.example.skinhealthai.data.network.RetrofitInstance
import com.example.skinhealthai.ui.theme.BluePrimary
import com.example.skinhealthai.ui.theme.BlueSecondary
import com.example.skinhealthai.ui.theme.LightGray
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    val apiService = remember { RetrofitInstance.api }
    val coroutineScope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var professionalId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cadastro",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = BluePrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nome de Usuário", color = BluePrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = BlueSecondary,
                cursorColor = BluePrimary,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = BluePrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = BlueSecondary,
                cursorColor = BluePrimary,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha", color = BluePrimary) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = BlueSecondary,
                cursorColor = BluePrimary,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = professionalId,
            onValueChange = { professionalId = it },
            label = { Text("ID Profissional", color = BluePrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = BlueSecondary,
                cursorColor = BluePrimary,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        HomeButton("Cadastrar") {
            coroutineScope.launch {
                val userRequest = UserRequest(
                    username = username,
                    email = email,
                    password = password,
                    professional_id = professionalId
                )

                try {
                    val response = apiService.registerUser(userRequest)
                    if (response.isSuccessful) {
                        val userResponse = response.body()
                        Toast.makeText(context, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                        println("Cadastro bem-sucedido. Token: ${userResponse?.token}")

                        navController.navigate("login") {
                            popUpTo("signup") { inclusive = true }
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(context, "Erro no cadastro: ${response.code()}", Toast.LENGTH_LONG).show()
                        println("Falha no cadastro. Código: ${response.code()}, Mensagem: ${response.message()}")
                        println("Corpo do erro: $errorBody")
                    }
                } catch (e: HttpException) {
                    Toast.makeText(context, "Erro de rede: ${e.message()}", Toast.LENGTH_LONG).show()
                    println("Erro de rede: ${e.message()}")
                } catch (e: Exception) {
                    Toast.makeText(context, "Erro inesperado: ${e.message}", Toast.LENGTH_LONG).show()
                    println("Ocorreu um erro inesperado: ${e.message}")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Já possui conta? Entrar",
            fontSize = 14.sp,
            color = BlueSecondary,
            modifier = Modifier
                .clickable {
                    navController.navigate("login") {
                        launchSingleTop = true
                    }
                }
                .padding(8.dp),
            textAlign = TextAlign.Center
        )
    }
}