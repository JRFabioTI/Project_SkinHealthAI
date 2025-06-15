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
    @SerializedName("images_with_analysis") val imagesWithAnalysis: List<FileImageWithAnalysisResponse>?
)

data class AnalysisResultData(
    val id: Int?, // ID do AnalysisResult
    val result: String?,
    val confidence: Float?,
    @SerializedName("model_version") val modelVersion: String?,
    // Adicione outros campos se o Django retornar, como id da imagem, user_created_by, datas
    @SerializedName("image") val imageId: Int?, // ID da imagem associada
    @SerializedName("user_created_by") val userCreatedBy: Int?,
    @SerializedName("date_created") val dateCreated: String?,
    @SerializedName("date_updated") val dateUpdated: String?,
    val error: String?
)

// NOVO: Classe para o objeto FileImageSkin aninhado com o resultado da análise
data class FileImageWithAnalysisResponse(
    val id: Int?, // ID do FileImageSkin
    val filename: String?,
    @SerializedName("remote_name") val remoteName: String?,
    @SerializedName("image_url") val imageUrl: String?, // Retornado pelo FileImageSkinSerializer
    val consultation: Int?, // ID da consulta associada
    @SerializedName("user_created_by") val userCreatedBy: Int?,
    @SerializedName("date_created") val dateCreated: String?,
    @SerializedName("date_updated") val dateUpdated: String?,
    @SerializedName("analysis_result") val analysisResult: AnalysisResultData? // Aninha o resultado da análise
)