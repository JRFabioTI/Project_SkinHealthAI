package com.example.skinhealthai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinhealthai.data.model.UserRequest
import com.example.skinhealthai.data.model.UserResponse
import com.example.skinhealthai.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UserUiState {
    object Idle : UserUiState()
    object Loading : UserUiState()
    data class Success(val user: UserResponse) : UserUiState()
    data class Error(val message: String) : UserUiState()
}

class UserViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _registerState = MutableStateFlow<UserUiState>(UserUiState.Idle)
    val registerState: StateFlow<UserUiState> = _registerState

    private val _userList = MutableStateFlow<List<UserResponse>>(emptyList())
    val userList: StateFlow<List<UserResponse>> = _userList

    fun registerUser(userRequest: UserRequest) {
        viewModelScope.launch {
            _registerState.value = UserUiState.Loading
            try {
                val response = repository.registerUser(userRequest)
                if (response.isSuccessful && response.body() != null) {
                    _registerState.value = UserUiState.Success(response.body()!!)
                } else {
                    val message = response.errorBody()?.string() ?: "Erro desconhecido no servidor"
                    _registerState.value = UserUiState.Error(message)
                }
            } catch (e: Exception) {
                _registerState.value = UserUiState.Error(e.message ?: "Erro inesperado")
            }
        }
    }

    fun getUsers() {
        viewModelScope.launch {
            try {
                val response = repository.getUsers()
                if (response.isSuccessful && response.body() != null) {
                    _userList.value = response.body()!!
                }
            } catch (_: Exception) {
            }
        }
    }

    fun resetRegisterState() {
        _registerState.value = UserUiState.Idle
    }
}
