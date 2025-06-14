package com.example.skinhealthai.data.model

import com.google.gson.annotations.SerializedName


data class ConsultationResponse(
    val id: Int,
    @SerializedName("agent")
    val agentId: Int,
    @SerializedName("patient")
    val patientId: Int,
    @SerializedName("patient_details")
    val patientDetails: PatientResponse?,
    @SerializedName("date_consultation")
    val dateConsultation: String,
    @SerializedName("photo_location")
    val photoLocation: String?,
    val notes: String?,
    @SerializedName("user_created_by")
    val userCreatedById: Int?,
    @SerializedName("date_created")
    val dateCreated: String,
    @SerializedName("date_modified")
    val dateModified: String,
    val active: Boolean,
    @SerializedName("file_image_urls") val fileImageUrls: List<String>?
)