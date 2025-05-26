package com.example.skinhealthai.data.model

import com.google.gson.annotations.SerializedName // Import necessário para mapeamento

data class UserRequest(
    @SerializedName("username") val username: String,
    val email: String,
    val password: String,
    val professional_id: String
)