package com.example.skinhealthai.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.*

object FileUtils {

    fun saveBitmapToFile(context: Context, bitmap: Bitmap): File {
        val file = File(context.cacheDir, "image.jpg")
        file.createNewFile()

        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }

        return file
    }

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap): File? {
        val filename = "skin_image_${System.currentTimeMillis()}.jpg"
        var savedFile: File? = null

        try {
            val outputStream: OutputStream?

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val imageUri: Uri? = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                imageUri?.let { uri ->
                    outputStream = context.contentResolver.openOutputStream(uri)
                    outputStream?.use {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    }

                    // Também salva uma cópia no cache para garantir retorno do File
                    val tempFile = File(context.cacheDir, filename)
                    FileOutputStream(tempFile).use { tempOut ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, tempOut)
                    }
                    savedFile = tempFile
                }

            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                if (!imagesDir.exists()) imagesDir.mkdirs()

                val imageFile = File(imagesDir, filename)
                outputStream = FileOutputStream(imageFile)
                outputStream.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }

                savedFile = imageFile
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return savedFile
    }
}
