package com.example.skinhealthai.data.model

import com.google.gson.annotations.SerializedName

data class UploadImageResponse(
    val id: Int?,
    val filename: String?,
    @SerializedName("remote_name") val remoteName: String?,
    val consultation: Int?, // ID da consulta (FK no FileImageSkin)
    @SerializedName("image_url") val imageUrl: String?, // Campo adicionado pelo FileImageSkinSerializer
    @SerializedName("user_created_by") val userCreatedBy: Int?,
    @SerializedName("date_created") val dateCreated: String?,
    @SerializedName("date_updated") val dateUpdated: String?,
    @SerializedName("analysis_result") val analysisResult: AnalysisResultData? // NOVO: Aninha o resultado da análise
)