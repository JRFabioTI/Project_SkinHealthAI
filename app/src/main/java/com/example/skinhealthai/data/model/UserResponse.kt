// com.example.skinhealthai.data.model/UserResponse.kt
package com.example.skinhealthai.data.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    // Se o ID do usuário também vem na resposta, adicione-o aqui
    val id: Int? = null, // Torne nullable se não tiver certeza se sempre virá
    val email: String? = null, // Torne nullable se não tiver certeza se sempre virá
    val token: String,
    @SerializedName("refresh")
    val refreshToken: String, // Mapeia o campo "refresh" do JSON

    @SerializedName("access")
    val accessToken: String   // Mapeia o campo "access" do JSON
)