package com.example.skinhealthai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinhealthai.repository.ImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

data class UploadUiState(
    val isLoading: Boolean = false,
    val result: String? = null,
    val error: String? = null
)

class ImageUploadViewModel : ViewModel() {
    var capturedBitmap: Bitmap? = null
    private val repository = ImageRepository()

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState

    fun uploadImage(file: File) {
        _uiState.value = UploadUiState(isLoading = true)

        viewModelScope.launch {
            try {
                val response = repository.uploadImageToApi(file)
                if (response != null) {
                    _uiState.value = UploadUiState(result = response.result)
                } else {
                    _uiState.value = UploadUiState(error = "Erro ao processar imagem")
                }
            } catch (e: Exception) {
                _uiState.value = UploadUiState(error = e.message ?: "Erro desconhecido")
            }
        }
    }
}
