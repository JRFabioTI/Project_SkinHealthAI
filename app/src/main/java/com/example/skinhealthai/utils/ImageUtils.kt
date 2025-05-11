package com.example.skinhealthai.utils

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

fun saveBitmapToFile(context: Context, bitmap: Bitmap): File? {
    return try {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "SkinImage_$timestamp.jpg"
        val file = File(context.cacheDir, fileName)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
