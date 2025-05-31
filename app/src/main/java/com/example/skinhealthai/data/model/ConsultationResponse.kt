package com.example.skinhealthai.data.model

import com.google.gson.annotations.SerializedName

data class ConsultationResponse(
    val id: Int,
    @SerializedName("agent")
    val agentId: Int, // ID do agente
    @SerializedName("patient")
    val patientId: Int, // ID do paciente
    @SerializedName("date_consultation")
    val dateConsultation: String, // Data e hora da consulta, String ISO 8601
    @SerializedName("photo_location")
    val photoLocation: String?,
    val notes: String?,
    @SerializedName("user_created_by")
    val userCreatedById: Int?,
    @SerializedName("date_created")
    val dateCreated: String,
    @SerializedName("date_modified")
    val dateModified: String,
    val active: Boolean
)
