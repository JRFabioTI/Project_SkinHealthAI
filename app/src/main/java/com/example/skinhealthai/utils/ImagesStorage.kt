package com.example.skinhealthai.utils

import android.graphics.Bitmap

object ImageStorage {
    private val images = mutableListOf<Bitmap>()

    fun setImages(list: List<Bitmap>) {
        images.clear()
        images.addAll(list)
    }

    fun getImage(index: Int): Bitmap? {
        return images.getOrNull(index)
    }

    fun getAll(): List<Bitmap> {
        return images
    }
}
