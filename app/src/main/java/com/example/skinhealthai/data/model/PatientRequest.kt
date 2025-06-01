package com.example.skinhealthai.data.model

data class PatientRequest(
    val name: String,
    val date_of_birth: String?,
    val gender: String?,
    val cellphone: String?,
    val cpf: String?,
    val email: String?
)