package com.example.skinhealthai.repository

import com.example.skinhealthai.data.model.Patient
import com.example.skinhealthai.data.model.PatientRequest
import com.example.skinhealthai.data.model.PatientResponse
import com.example.skinhealthai.data.network.ApiService
import com.example.skinhealthai.data.network.RetrofitInstance
import retrofit2.Response

class PatientRepository {
    private val api = RetrofitInstance.api

    suspend fun getAllPatients(): Response<List<PatientResponse>> {
        return api.getPatients()
    }

    suspend fun getPatientById(id: Int): Response<PatientResponse> {
        return api.getPatient(id)
    }

    suspend fun createPatient(patient: PatientRequest): Response<PatientResponse> {
        return api.createPatient(patient)
    }

    suspend fun updatePatient(id: Int, patient: PatientRequest): Response<PatientResponse> {
        return api.updatePatient(id, patient)
    }

    suspend fun deletePatient(id: Int): Response<Unit> {
        return api.deletePatient(id)
    }
}
