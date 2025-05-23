package com.example.skinhealthai.data.network

import com.example.skinhealthai.data.UserLoginRequest
import com.example.skinhealthai.data.model.UserRequest
import com.example.skinhealthai.data.model.UserResponse
import com.example.skinhealthai.data.model.models.Patient
import com.example.skinhealthai.data.model.models.PredictionResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Upload de imagem para predição
    @Multipart
    @POST("api/skin/consultation/upload_file/")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<PredictionResponse>

    // Autenticação
    @POST("api/token/")
    suspend fun login(
        @Body request: UserLoginRequest
    ): Response<UserResponse>

    // Cadastro de usuário
    @POST("api/skin/user/")
    suspend fun registerUser(
        @Body userRequest: UserRequest
    ): Response<UserResponse>

    // CRUD de usuários
    @GET("api/skin/user/")
    suspend fun getUsers(): Response<List<UserResponse>>

    @GET("api/skin/user/{id}/")
    suspend fun getUserById(@Path("id") id: Int): Response<UserResponse>

    @PUT("api/skin/user/{id}/")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body userRequest: UserRequest
    ): Response<UserResponse>

    @DELETE("api/skin/user/{id}/")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>

    // CRUD de pacientes
    @GET("api/skin/patient/")
    suspend fun getPatients(): Response<List<Patient>>

    @GET("api/skin/patient/{id}/")
    suspend fun getPatient(@Path("id") id: Int): Response<Patient>

    @POST("api/skin/patient/")
    suspend fun createPatient(@Body patient: Patient): Response<Patient>

    @PUT("api/skin/patient/{id}/")
    suspend fun updatePatient(
        @Path("id") id: Int,
        @Body patient: Patient
    ): Response<Patient>

    @DELETE("api/skin/patient/{id}/")
    suspend fun deletePatient(@Path("id") id: Int): Response<Unit>
}
