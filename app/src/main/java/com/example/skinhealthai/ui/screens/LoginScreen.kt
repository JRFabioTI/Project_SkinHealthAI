package com.example.skinhealthai.ui.screens

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.skinhealthai.viewmodel.LoginViewModel
import com.example.skinhealthai.viewmodel.LoginState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
// --- NOVOS IMPORTS AQUI ---
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
// --- FIM DOS NOVOS IMPORTS ---
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation // Importar VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.skinhealthai.ui.theme.BluePrimary
import com.example.skinhealthai.ui.theme.BlueSecondary
import com.example.skinhealthai.ui.theme.LightGray
import com.example.skinhealthai.utils.AuthTokenManager

@Composable
fun LoginScreen(navController: NavController, loginViewModel: LoginViewModel = viewModel()) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val loginState by loginViewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                val userResponse = (loginState as LoginState.Success).user
                AuthTokenManager.saveAuthToken(context, userResponse.accessToken)

                snackbarHostState.showSnackbar("Login bem-sucedido!")
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
                loginViewModel.resetState()
            }
            is LoginState.Error -> {
                snackbarHostState.showSnackbar((loginState as LoginState.Error).message)
                loginViewModel.resetState()
            }
            else -> {
                // Para Idle e Loading, não faz nada específico aqui
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Title()

            Spacer(modifier = Modifier.height(32.dp))

            UsernameInput(username = username, onUsernameChange = { username = it })

            Spacer(modifier = Modifier.height(16.dp))

            PasswordInput(
                password = password,
                onPasswordChange = { password = it },
                onDoneAction = {
                    focusManager.clearFocus()
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            LoginButton(
                enabled = username.isNotBlank() && password.isNotBlank() && loginState !is LoginState.Loading,
                onClick = {
                    focusManager.clearFocus()
                    loginViewModel.login(username, password)
                }
            )

            if (loginState is LoginState.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = BluePrimary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            SignUpPrompt {
                navController.navigate("signup") {
                    launchSingleTop = true
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun Title() {
    Text(
        text = "Entrar",
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        color = BluePrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun UsernameInput(username: String, onUsernameChange: (String) -> Unit) {
    OutlinedTextField(
        value = username,
        onValueChange = onUsernameChange,
        label = { Text("Username", color = BluePrimary) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BluePrimary,
            unfocusedBorderColor = BlueSecondary,
            cursorColor = BluePrimary,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
}

@Composable
fun PasswordInput(
    password: String,
    onPasswordChange: (String) -> Unit,
    onDoneAction: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Senha", color = BluePrimary) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDoneAction() }),
        trailingIcon = {
            val image = if (passwordVisible)
                Icons.Filled.Visibility
            else Icons.Filled.VisibilityOff

            val description = if (passwordVisible) "Esconder senha" else "Mostrar senha"

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = description)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BluePrimary,
            unfocusedBorderColor = BlueSecondary,
            cursorColor = BluePrimary,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
}

@Composable
fun LoginButton(enabled: Boolean, onClick: () -> Unit) {
    HomeButton(
        text = "Entrar",
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    )
}

@Composable
fun SignUpPrompt(onClick: () -> Unit) {
    Text(
        text = "Não tem uma conta? Cadastre-se",
        fontSize = 14.sp,
        color = BlueSecondary,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun HomeButton(
    text: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BluePrimary,
            disabledContainerColor = BluePrimary.copy(alpha = 0.4f),
            contentColor = Color.White
        )
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}