package com.example.skinhealthai.ui.viewmodel

sealed class ImageProcessState {
    object Idle : ImageProcessState()
    object Loading : ImageProcessState()
    data class Success(val imageId: Int, val resultId: Int) : ImageProcessState()
    data class Error(val message: String) : ImageProcessState()
}