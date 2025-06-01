package com.example.skinhealthai.repository

import com.example.skinhealthai.data.model.UserRequest
import com.example.skinhealthai.data.model.UserResponse
import com.example.skinhealthai.data.network.RetrofitInstance
import retrofit2.Response

class UserRepository {

    private val api = RetrofitInstance.api

    suspend fun registerUser(userRequest: UserRequest): Response<UserResponse> {
        return api.registerUser(userRequest)
    }

    suspend fun getUsers(): Response<List<UserResponse>> {
        return api.getUsers()
    }

    suspend fun getUserById(userId: Int): Response<UserResponse> {
        return api.getUserById(userId)
    }

    suspend fun updateUser(userId: Int, userRequest: UserRequest): Response<UserResponse> {
        return api.updateUser(userId, userRequest)
    }

    suspend fun deleteUser(userId: Int): Response<Unit> {
        return api.deleteUser(userId)
    }
}
