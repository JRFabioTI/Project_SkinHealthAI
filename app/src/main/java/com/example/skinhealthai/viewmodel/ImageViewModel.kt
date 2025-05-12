package com.example.skinhealthai.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

// O ViewModel gerencia o estado das imagens capturadas
class ImageViewModel : ViewModel() {
    val images = mutableStateOf<List<Bitmap>>(listOf())

    fun addImage(image: Bitmap) {
        images.value = images.value + image
    }
}
