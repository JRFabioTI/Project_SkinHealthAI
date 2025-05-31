package com.example.skinhealthai.data.model

data class PatientRequest(
    val name: String,
    val date_of_birth: String?, // Pode ser nulo
    val gender: String?, // Pode ser nulo, mapeie para 'M', 'F', 'O' depois
    val cellphone: String?,
    val cpf: String?,
    val email: String?
)