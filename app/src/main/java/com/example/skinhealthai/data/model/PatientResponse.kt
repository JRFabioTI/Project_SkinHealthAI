package com.example.skinhealthai.data.model

data class PatientResponse(
    val id: Int,
    val name: String,
    val date_of_birth: String?,
    val gender: String?,
    val cellphone: String?,
    val cpf: String?,
    val email: String?,
    val user_created_by: Int?
)
