package com.example.skinhealthai.data.model

import com.google.gson.annotations.SerializedName

data class UploadImageResponse(
    val message: String,
    @SerializedName("consultation_id") val consultationId: String
)
