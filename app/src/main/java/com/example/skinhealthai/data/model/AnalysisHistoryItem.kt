package com.example.skinhealthai.data.model

data class AnalysisHistoryItem(
    val id: String,
    val patientId: Int,
    val patientName: String,
    val date: String,
    val diagnosis: String,
    val risk: String,

)