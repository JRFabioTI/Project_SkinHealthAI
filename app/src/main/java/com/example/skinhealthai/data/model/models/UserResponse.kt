package com.example.skinhealthai.data.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    val id: Int,
    @SerializedName("username") val name: String,
    val email: String,
    val token: String
)
