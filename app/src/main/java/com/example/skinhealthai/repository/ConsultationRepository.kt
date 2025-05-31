package com.example.skinhealthai.repository

import com.example.skinhealthai.data.model.ConsultationRequest
import com.example.skinhealthai.data.model.ConsultationResponse
import com.example.skinhealthai.data.network.ApiService // Garanta que este seja o caminho correto para sua ApiService
import com.example.skinhealthai.data.network.RetrofitInstance
import retrofit2.Response

class ConsultationRepository {
    private val api = RetrofitInstance.api // Supondo que 'api' seja uma instância de ApiService

    suspend fun getAllConsultations(): Response<List<ConsultationResponse>> {
        return api.getConsultations()
    }

    suspend fun getConsultationById(id: Int): Response<ConsultationResponse> {
        return api.getConsultation(id)
    }

    suspend fun createConsultation(consultation: ConsultationRequest): Response<ConsultationResponse> {
        return api.createConsultation(consultation)
    }

    suspend fun updateConsultation(id: Int, consultation: ConsultationRequest): Response<ConsultationResponse> {
        return api.updateConsultation(id, consultation)
    }

    suspend fun deleteConsultation(id: Int): Response<Unit> {
        return api.deleteConsultation(id)
    }

    // --- NOVO: Método para obter consultas por ID do paciente ---
    suspend fun getConsultationsByPatientId(patientId: Int): Response<List<ConsultationResponse>> {
        return api.getConsultationsByPatientId(patientId)
    }
}