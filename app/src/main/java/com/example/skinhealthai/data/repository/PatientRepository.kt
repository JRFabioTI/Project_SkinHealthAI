package com.example.skinhealthai.data.repository

import com.example.skinhealthai.data.model.models.Patient
import com.example.skinhealthai.data.network.ApiService
import retrofit2.Response

class PatientRepository(private val apiService: ApiService) {

    suspend fun getAllPatients(): Response<List<Patient>> {
        return apiService.getPatients()
    }

    suspend fun getPatientById(id: Int): Response<Patient> {
        return apiService.getPatient(id)
    }

    suspend fun createPatient(patient: Patient): Response<Patient> {
        return apiService.createPatient(patient)
    }

    suspend fun updatePatient(id: Int, patient: Patient): Response<Patient> {
        return apiService.updatePatient(id, patient)
    }

    suspend fun deletePatient(id: Int): Response<Unit> {
        return apiService.deletePatient(id)
    }
}
