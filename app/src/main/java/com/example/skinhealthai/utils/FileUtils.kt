package com.example.skinhealthai.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri // Importe Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception // Importe Exception para lidar com erros

object FileUtils {

    /**
     * Salva um Bitmap em um arquivo temporário no diretório de cache do aplicativo.
     * Este arquivo é ideal para ser usado com FileProvider.
     * Retorna o File? salvo ou null em caso de erro.
     */
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, quality: Int = 100): File? {
        // Gera um nome de arquivo único para evitar sobreescrita
        val filename = "image_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, filename)

        return try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }
            file // Retorna o arquivo criado se tudo ocorrer bem
        } catch (e: Exception) {
            e.printStackTrace() // Imprime o erro para depuração
            null // Retorna null se houver qualquer exceção
        }
    }

    /**
     * Salva um Bitmap na galeria de imagens pública do dispositivo.
     * Esta função não é destinada a ser a fonte do File para o FileProvider,
     * use saveBitmapToFile para isso.
     * Retorna o Uri? da imagem na galeria ou null em caso de erro.
     */
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, quality: Int = 100): Uri? {
        val filename = "skin_image_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    // Use um subdiretório para organizar melhor na galeria
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "SkinHealthAI_Images")
                }
                imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { context.contentResolver.openOutputStream(it) }
            } else {
                // Para APIs < Q, salvamos diretamente no diretório público
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                // Crie um subdiretório para organizar
                val appSpecificDir = File(imagesDir, "SkinHealthAI_Images").apply {
                    mkdirs() // Garante que o diretório exista
                }
                val imageFile = File(appSpecificDir, filename)
                fos = FileOutputStream(imageFile)
                imageUri = Uri.fromFile(imageFile) // Obtém um Uri para o arquivo
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it)
            }
            return imageUri // Retorna o Uri da imagem salva na galeria
        } catch (e: Exception) {
            e.printStackTrace() // Imprime o erro para depuração
            return null // Retorna null se houver qualquer exceção
        } finally {
            try {
                fos?.close() // Garante que o stream seja fechado
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}