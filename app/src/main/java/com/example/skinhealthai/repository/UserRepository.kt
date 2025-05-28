package com.example.skinhealthai.repository

import com.example.skinhealthai.data.model.UserRequest
import com.example.skinhealthai.data.model.UserResponse
import com.example.skinhealthai.data.network.RetrofitInstance
import retrofit2.Response

class UserRepository {

    private val api = RetrofitInstance.api

    // Cadastrar novo usuário
    suspend fun registerUser(userRequest: UserRequest): Response<UserResponse> {
        return api.registerUser(userRequest)
    }

    // Listar todos os usuários
    suspend fun getUsers(): Response<List<UserResponse>> {
        return api.getUsers()
    }

    // Buscar usuário por ID
    suspend fun getUserById(userId: Int): Response<UserResponse> {
        return api.getUserById(userId)
    }

    // Atualizar usuário por ID
    suspend fun updateUser(userId: Int, userRequest: UserRequest): Response<UserResponse> {
        return api.updateUser(userId, userRequest)
    }

    // Deletar usuário por ID
    suspend fun deleteUser(userId: Int): Response<Unit> {
        return api.deleteUser(userId)
    }
}
