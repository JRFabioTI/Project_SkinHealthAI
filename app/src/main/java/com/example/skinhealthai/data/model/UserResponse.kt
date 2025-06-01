package com.example.skinhealthai.data.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    val id: Int? = null,
    val email: String? = null,
    val token: String,
    @SerializedName("user_name")
    val userName: String? = null,
    @SerializedName("refresh")
    val refreshToken: String,
    @SerializedName("access")
    val accessToken: String
)