package com.example.skinhealthai.repository

import com.example.skinhealthai.data.network.RetrofitInstance
import com.example.skinhealthai.data.model.PredictionResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ImageRepository {

    suspend fun uploadImageToApi(imageFile: File): PredictionResponse? {
        val mediaType = "image/*".toMediaTypeOrNull()
        val requestBody = imageFile.asRequestBody(mediaType)

        val imagePart = MultipartBody.Part.createFormData(
            name = "image",
            filename = imageFile.name,
            body = requestBody
        )

        val response = RetrofitInstance.api.uploadImage(imagePart)
        return if (response.isSuccessful) response.body() else null
    }
}
