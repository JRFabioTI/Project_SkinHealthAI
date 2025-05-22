package com.example.skinhealthai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinhealthai.data.model.UserRequest
import com.example.skinhealthai.data.model.UserResponse
import com.example.skinhealthai.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class UserViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    // Estados para UI
    private val _userResponse = MutableStateFlow<Response<UserResponse>?>(null)
    val userResponse: StateFlow<Response<UserResponse>?> = _userResponse

    private val _userListResponse = MutableStateFlow<Response<List<UserResponse>>?>(null)
    val userListResponse: StateFlow<Response<List<UserResponse>>?> = _userListResponse

    // Função para registrar usuário
    fun registerUser(userRequest: UserRequest) {
        viewModelScope.launch {
            val response = repository.registerUser(userRequest)
            _userResponse.value = response
        }
    }

    // Função para listar usuários
    fun getUsers() {
        viewModelScope.launch {
            val response = repository.getUsers()
            _userListResponse.value = response
        }
    }
}
