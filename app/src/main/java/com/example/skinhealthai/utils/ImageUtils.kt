package com.example.skinhealthai.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import java.io.OutputStream

fun saveBitmapToGallery(context: Context, bitmap: Bitmap?, displayName: String = "captured_image") {
    val resolver = context.contentResolver
    val imageCollection =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.WIDTH, bitmap?.width)
        put(MediaStore.Images.Media.HEIGHT, bitmap?.height)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val imageUri = resolver.insert(imageCollection, contentValues)
    imageUri?.let { uri ->
        val outputStream: OutputStream? = resolver.openOutputStream(uri)
        outputStream?.use {
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }

        // Forçar a atualização da galeria
        context.contentResolver.notifyChange(uri, null)

        // Marcar a imagem como disponível após a escrita (para Android 10 e superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        // Exibir Toast para confirmação
        Toast.makeText(context, "Imagem salva na galeria", Toast.LENGTH_SHORT).show()
    } ?: run {
        Toast.makeText(context, "Erro ao salvar imagem", Toast.LENGTH_SHORT).show()
    }
}