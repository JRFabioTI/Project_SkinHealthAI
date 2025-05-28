package com.example.skinhealthai.repository

import com.example.skinhealthai.data.network.RetrofitInstance
import com.example.skinhealthai.data.model.PredictionResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class ImageRepository {
    suspend fun uploadImageToApi(imageFile: File): PredictionResponse? {
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
        val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
        val response = RetrofitInstance.api.uploadImage(body)
        return if (response.isSuccessful) response.body() else null
    }
}