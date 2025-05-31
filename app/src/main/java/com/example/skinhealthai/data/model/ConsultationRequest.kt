package com.example.skinhealthai.data.model

import com.google.gson.annotations.SerializedName

data class ConsultationRequest(
    @SerializedName("patient") // O ID do paciente que você selecionou
    val patientId: Int,
    @SerializedName("date_consultation")
    val dateConsultation: String, // Formato "yyyy-MM-dd'T'HH:mm:ssZ" ou similar (ISO 8601)
    @SerializedName("photo_location")
    val photoLocation: String?,
    val notes: String?
)
