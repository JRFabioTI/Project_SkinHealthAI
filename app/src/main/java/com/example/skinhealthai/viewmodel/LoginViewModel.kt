package com.example.skinhealthai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinhealthai.data.model.UserLoginRequest
import com.example.skinhealthai.data.model.UserResponse
import com.example.skinhealthai.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: UserResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                val user = repository.login(UserLoginRequest(email, password))
                _loginState.value = LoginState.Success(user)
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
