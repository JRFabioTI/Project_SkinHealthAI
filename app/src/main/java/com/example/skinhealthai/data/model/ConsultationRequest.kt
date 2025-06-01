package com.example.skinhealthai.data.model

import com.google.gson.annotations.SerializedName

data class ConsultationRequest(
    @SerializedName("patient")
    val patientId: Int,
    @SerializedName("date_consultation")
    val dateConsultation: String,
    @SerializedName("photo_location")
    val photoLocation: String?,
    val notes: String?
)
