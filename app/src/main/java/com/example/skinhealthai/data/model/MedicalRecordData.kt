package com.example.skinhealthai.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class PatientRecordPdfData(
    val name: String,
    val email: String?,
    val cpf: String?,
    val gender: String?,
    val dateOfBirth: String?,
    val age: String?,
    val cellphone: String?
) {
    companion object {
        fun fromPatientResponse(patient: PatientResponse): PatientRecordPdfData {
            val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val apiPatientDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            val formattedDateOfBirth = patient.date_of_birth?.let { apiDateString ->
                try {
                    apiPatientDateFormat.parse(apiDateString)?.let { dateObject ->
                        displayDateFormat.format(dateObject)
                    }
                } catch (e: Exception) { null }
            }

            val calculatedAge = patient.date_of_birth?.let { dobString ->
                try {
                    val parsedDate = apiPatientDateFormat.parse(dobString)
                    if (parsedDate != null) {
                        val dobCalendar = Calendar.getInstance().apply { time = parsedDate }
                        val todayCalendar = Calendar.getInstance()

                        var ageValue = todayCalendar.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)
                        if (todayCalendar.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
                            ageValue--
                        }
                        "$ageValue anos"
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }

            val fullGenderText = patient.gender?.let { genderValue ->
                when(genderValue.uppercase(Locale.getDefault())) {
                    "M", "MASCULINO" -> "Masculino"
                    "F", "FEMININO" -> "Feminino"
                    "O", "OUTRO" -> "Outro"
                    else -> genderValue
                }
            }

            return PatientRecordPdfData(
                name = patient.name,
                email = patient.email,
                cpf = patient.cpf,
                gender = fullGenderText,
                dateOfBirth = formattedDateOfBirth,
                age = calculatedAge,
                cellphone = patient.cellphone
            )
        }
    }
}

data class AppointmentPdfData(
    val dateConsultation: String,
    val photoLocation: String?,
    val notes: String?
) {
    companion object {
        fun fromConsultationResponse(consultation: ConsultationResponse): AppointmentPdfData {
            val displayDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val apiDateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())

            val formattedConsultationDate = try {
                apiDateTimeFormat.parse(consultation.dateConsultation)?.let { dateObject ->
                    displayDateFormat.format(dateObject)
                }
            } catch (e: Exception) { "Data/Hora Inválida" }

            return AppointmentPdfData(
                dateConsultation = formattedConsultationDate ?: "Data/Hora Inválida",
                photoLocation = consultation.photoLocation,
                notes = consultation.notes
            )
        }
    }
}

data class MedicalRecordPdfContent(
    val patientData: PatientRecordPdfData,
    val appointments: List<AppointmentPdfData>
)