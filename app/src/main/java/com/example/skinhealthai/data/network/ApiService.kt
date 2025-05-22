package com.example.skinhealthai.data.network

import com.example.skinhealthai.data.UserLoginRequest
import com.example.skinhealthai.data.model.UserRequest
import com.example.skinhealthai.data.model.UserResponse
import com.example.skinhealthai.data.model.models.Patient
import com.example.skinhealthai.data.model.models.PredictionResponse
import com.example.skinhealthai.data.model.models.UserRegisterRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // uploadImage
    @Multipart
    @POST("upload/")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<PredictionResponse>

    // Usuários
    @POST("api/skin/users/")
    suspend fun registerUser(
        @Body userRequest: UserRequest
    ): Response<UserResponse>

    @GET("api/skin/users/")
    suspend fun getUsers(): Response<List<UserResponse>>

    @GET("api/skin/users/{id}/")
    suspend fun getUserById(
        @Path("id") id: Int
    ): Response<UserResponse>

    @PUT("api/skin/users/{id}/")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body userRequest: UserRequest
    ): Response<UserResponse>

    @DELETE("api/skin/users/{id}/")
    suspend fun deleteUser(
        @Path("id") id: Int
    ): Response<Unit>

    // Pacientes
    @GET("api/skin/patients/")
    suspend fun getPatients(): Response<List<Patient>>

    @GET("api/skin/patients/{id}/")
    suspend fun getPatient(
        @Path("id") id: Int
    ): Response<Patient>

    @POST("api/skin/patients/")
    suspend fun createPatient(
        @Body patient: Patient
    ): Response<Patient>

    @PUT("api/skin/patients/{id}/")
    suspend fun updatePatient(
        @Path("id") id: Int,
        @Body patient: Patient
    ): Response<Patient>

    @DELETE("api/skin/patients/{id}/")
    suspend fun deletePatient(
        @Path("id") id: Int
    ): Response<Unit>

    @POST("auth/login/")
    suspend fun login(@Body request: UserLoginRequest): Response<UserResponse>

    @POST("auth/register/")
    suspend fun register(@Body request: UserRegisterRequest): Response<UserResponse>
}
