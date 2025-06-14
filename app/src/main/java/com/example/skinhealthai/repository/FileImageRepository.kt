package com.example.skinhealthai.repository

import com.example.skinhealthai.data.network.ApiService
import com.example.skinhealthai.data.network.RetrofitInstance
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.example.skinhealthai.data.model.UploadImageResponse
import retrofit2.Response

class FileImageRepository(private val apiService: ApiService = RetrofitInstance.api) {

    suspend fun uploadImageToMinio(
        fileObj: MultipartBody.Part,
        consultationId: RequestBody
    ): Response<UploadImageResponse> {
        return apiService.uploadImageToMinio(fileObj, consultationId)
    }
}