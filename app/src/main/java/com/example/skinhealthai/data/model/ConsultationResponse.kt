// com.example.skinhealthai.data.model/ConsultationResponse.kt
package com.example.skinhealthai.data.model

import com.google.gson.annotations.SerializedName

// Certifique-se de que PatientResponse esteja importado e definido em algum lugar
// Exemplo (se não estiver no mesmo arquivo):
// import com.example.skinhealthai.data.model.PatientResponse

data class ConsultationResponse(
    val id: Int,
    @SerializedName("agent")
    val agentId: Int, // ID do agente
    @SerializedName("patient")
    val patientId: Int, // ID do paciente

    // NOVO: Mapeie o patient_details que vem do seu Django ConsultationSerializer
    @SerializedName("patient_details")
    val patientDetails: PatientResponse?, // <-- Adicione este campo
    // Torne-o anulável (PatientResponse?) caso a API possa não enviá-lo
    // ou se você não tiver certeza

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