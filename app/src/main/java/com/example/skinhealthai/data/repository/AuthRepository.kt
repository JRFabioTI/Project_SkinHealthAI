package com.example.skinhealthai.data.repository

import com.example.skinhealthai.data.UserLoginRequest
import com.example.skinhealthai.data.model.UserRequest
import com.example.skinhealthai.data.model.UserResponse
import com.example.skinhealthai.data.network.RetrofitInstance
import retrofit2.HttpException
import java.io.IOException

class AuthRepository {

    private val api = RetrofitInstance.api

    suspend fun login(request: UserLoginRequest): UserResponse {
        try {
            val response = api.login(request)
            if (response.isSuccessful) {
                return response.body() ?: throw Exception("Resposta vazia do servidor")
            } else {
                throw HttpException(response)
            }
        } catch (e: IOException) {
            throw Exception("Falha na conexão. Verifique sua internet.")
        } catch (e: HttpException) {
            throw Exception("Erro no login: ${e.message()}")
        } catch (e: Exception) {
            throw Exception(e.message ?: "Erro desconhecido no login")
        }
    }

    suspend fun register(request: UserRequest): UserResponse {
        try {
            val response = api.registerUser(request)
            if (response.isSuccessful) {
                return response.body() ?: throw Exception("Resposta vazia do servidor")
            } else {
                throw HttpException(response)
            }
        } catch (e: IOException) {
            throw Exception("Falha na conexão. Verifique sua internet.")
        } catch (e: HttpException) {
            throw Exception("Erro no cadastro: ${e.message()}")
        } catch (e: Exception) {
            throw Exception(e.message ?: "Erro desconhecido no cadastro")
        }
    }
}
