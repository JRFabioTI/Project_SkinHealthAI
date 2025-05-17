package com.example.skinhealthai.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinhealthai.data.repository.ImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class ImageUploadViewModel : ViewModel() {
    private val repository = ImageRepository()
    private val _predictionResult = MutableStateFlow<String?>(null)
    val predictionResult: StateFlow<String?> = _predictionResult

    fun uploadImage(file: File) {
        viewModelScope.launch {
            val response = repository.uploadImageToApi(file)
            _predictionResult.value = response?.result ?: "Erro ao analisar imagem"
        }
    }
}

