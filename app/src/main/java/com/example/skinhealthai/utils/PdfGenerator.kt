package com.example.skinhealthai.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.skinhealthai.data.model.MedicalRecordPdfContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {
    private fun drawTextWithLineBreaks(canvas: Canvas, paint: Paint, text: String, x: Float, y: Float, maxWidth: Float): Float {
        val lines = mutableListOf<String>()
        val words = text.split(" ")
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) < maxWidth) {
                currentLine.append(if (currentLine.isEmpty()) "" else " ").append(word)
            } else {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }
        lines.add(currentLine.toString())

        var currentY = y
        for (line in lines) {
            canvas.drawText(line, x, currentY, paint)
            currentY += paint.fontSpacing
        }
        return currentY
    }

    suspend fun generateMedicalRecordPdf(context: Context, medicalRecord: MedicalRecordPdfContent): Uri? = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()

        // Margens
        val margin = 40f
        val contentWidth = pageInfo.pageWidth - (2 * margin)
        var yPosition = margin

        // --- Título do Prontuário ---
        paint.color = Color.BLACK
        paint.textSize = 28f
        paint.isFakeBoldText = true // Negrito
        canvas.drawText("Prontuário Médico", margin, yPosition + paint.fontSpacing, paint)
        yPosition += 60f // Espaço após o título

        // Linha separadora
        paint.strokeWidth = 2f
        canvas.drawLine(margin, yPosition, pageInfo.pageWidth - margin, yPosition, paint)
        yPosition += 20f

        // --- Dados do Paciente ---
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Dados do Paciente:", margin, yPosition + paint.fontSpacing, paint)
        yPosition += 40f

        paint.textSize = 14f
        paint.isFakeBoldText = false
        val patient = medicalRecord.patientData

        canvas.drawText("Nome: ${patient.name}", margin, yPosition, paint)
        yPosition += paint.fontSpacing

        patient.email?.let {
            canvas.drawText("Email: $it", margin, yPosition, paint)
            yPosition += paint.fontSpacing
        }
        patient.cpf?.let {
            canvas.drawText("CPF: ${it}", margin, yPosition, paint)
            yPosition += paint.fontSpacing
        }
        patient.gender?.let {
            canvas.drawText("Gênero: $it", margin, yPosition, paint)
            yPosition += paint.fontSpacing
        }
        patient.dateOfBirth?.let { dob ->
            val ageText = patient.age?.let { " (Idade: $it)" } ?: ""
            canvas.drawText("Data de Nascimento: $dob$ageText", margin, yPosition, paint)
            yPosition += paint.fontSpacing
        }
        patient.cellphone?.let {
            canvas.drawText("Telefone: $it", margin, yPosition, paint)
            yPosition += paint.fontSpacing
        }

        yPosition += 30f

        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Histórico de Consultas:", margin, yPosition + paint.fontSpacing, paint)
        yPosition += 40f

        if (medicalRecord.appointments.isEmpty()) {
            paint.textSize = 14f
            paint.isFakeBoldText = false
            canvas.drawText("Nenhuma consulta registrada.", margin, yPosition, paint)
        } else {
            // Ordenar consultas da mais recente para a mais antiga
            val sortedAppointments = medicalRecord.appointments.sortedByDescending {
                try {
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse(it.dateConsultation) ?: Date(0)
                } catch (e: Exception) {
                    Date(0)
                }
            }

            sortedAppointments.forEachIndexed { index, appointment ->
                val estimatedAppointmentHeight = 250f
                if (yPosition + estimatedAppointmentHeight > pageInfo.pageHeight - margin) {
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = margin

                    paint.color = Color.BLACK
                    paint.textSize = 24f
                    paint.isFakeBoldText = true
                    canvas.drawText("Prontuário Médico (Continuação)", margin, yPosition, paint)
                    yPosition += 40f
                }

                paint.textSize = 16f
                paint.isFakeBoldText = true
                canvas.drawText("Consulta ${index + 1}: ${appointment.dateConsultation}", margin, yPosition, paint)
                yPosition += 25f

                paint.textSize = 14f
                paint.isFakeBoldText = false
                appointment.notes?.let { notes ->
                    canvas.drawText("Notas:", margin, yPosition, paint)
                    yPosition += paint.fontSpacing
                    yPosition = drawTextWithLineBreaks(canvas, paint, notes, margin + 10, yPosition, contentWidth - 10)
                    yPosition += 10f
                }

                appointment.photoLocation?.let { photoUrl ->
                    if (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) {
                        try {
                            paint.isFakeBoldText = true
                            canvas.drawText("Local da Lesão:", margin, yPosition, paint)
                            yPosition += 25f
                            paint.isFakeBoldText = false


                            val bitmap = downloadImage(photoUrl)
                            if (bitmap != null) {
                                val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                                val maxImageWidth = contentWidth * 0.7f
                                var imageHeight = maxImageWidth / aspectRatio
                                var imageWidth = maxImageWidth

                                if (imageHeight > 200) {
                                    imageHeight = 200f
                                    imageWidth = imageHeight * aspectRatio
                                }

                                val imageRect = RectF(margin, yPosition, margin + imageWidth, yPosition + imageHeight)
                                canvas.drawBitmap(bitmap, null, imageRect, null)
                                yPosition += imageHeight + 20f
                                bitmap.recycle()
                            } else {
                                paint.color = Color.RED
                                canvas.drawText("Erro ao carregar imagem da URL: $photoUrl", margin, yPosition, paint)
                                paint.color = Color.BLACK
                                yPosition += 20f
                            }
                        } catch (e: Exception) {
                            paint.color = Color.RED
                            canvas.drawText("Erro ao carregar imagem: ${e.message}", margin, yPosition, paint)
                            paint.color = Color.BLACK
                            yPosition += 20f
                        }
                    } else {
                        canvas.drawText("Local da Lesão: $photoUrl", margin, yPosition, paint)
                        yPosition += 20f
                    }
                }
                yPosition += 30f
            }
        }

        pdfDocument.finishPage(page)

        val fileName = "prontuario_${medicalRecord.patientData.name.replace(" ", "_")}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
        val file = File(context.cacheDir, fileName)

        try {
            FileOutputStream(file).use { fos ->
                pdfDocument.writeTo(fos)
            }
            pdfDocument.close()
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun downloadImage(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection()
            connection.connect()
            val input: InputStream = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}