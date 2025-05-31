// com.example.skinhealthai.ui.viewmodel/ConsultationViewModel.kt (ou mantenha PatientRecordViewModel.kt)
package com.example.skinhealthai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinhealthai.data.model.ConsultationRequest
import com.example.skinhealthai.data.model.PatientResponse
import com.example.skinhealthai.repository.PatientRepository
import com.example.skinhealthai.repository.ConsultationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Estados para a UI da tela de consulta (carregamento do paciente)
sealed class PatientDataUiState { // Renomeado para mais clareza para esta tela
    object Loading : PatientDataUiState()
    data class PatientLoaded(val patient: PatientResponse) : PatientDataUiState()
    data class Error(val message: String) : PatientDataUiState()
}

// Estados para a operação de criação de consulta
sealed class ConsultationCreationState {
    object Idle : ConsultationCreationState()
    object Loading : ConsultationCreationState()
    object Success : ConsultationCreationState()
    data class Error(val message: String) : ConsultationCreationState()
}

class ConsultationViewModel( // Renomeado a classe para refletir o propósito
    private val patientRepository: PatientRepository = PatientRepository(),
    private val consultationRepository: ConsultationRepository = ConsultationRepository()
) : ViewModel() {

    private val _patientDataUiState = MutableStateFlow<PatientDataUiState>(PatientDataUiState.Loading)
    val patientDataUiState: StateFlow<PatientDataUiState> = _patientDataUiState

    private val _consultationCreationState = MutableStateFlow<ConsultationCreationState>(ConsultationCreationState.Idle)
    val consultationCreationState: StateFlow<ConsultationCreationState> = _consultationCreationState

    fun loadPatient(patientId: Int) {
        viewModelScope.launch {
            _patientDataUiState.value = PatientDataUiState.Loading
            try {
                val response = patientRepository.getPatientById(patientId)
                if (response.isSuccessful && response.body() != null) {
                    _patientDataUiState.value = PatientDataUiState.PatientLoaded(response.body()!!)
                } else {
                    val message = response.errorBody()?.string() ?: "Paciente não encontrado."
                    _patientDataUiState.value = PatientDataUiState.Error(message)
                }
            } catch (e: Exception) {
                _patientDataUiState.value = PatientDataUiState.Error(e.message ?: "Erro ao carregar paciente.")
            }
        }
    }

    fun createConsultation(consultationRequest: ConsultationRequest) {
        viewModelScope.launch {
            _consultationCreationState.value = ConsultationCreationState.Loading
            try {
                val response = consultationRepository.createConsultation(consultationRequest)
                if (response.isSuccessful && response.body() != null) {
                    _consultationCreationState.value = ConsultationCreationState.Success
                } else {
                    val message = response.errorBody()?.string() ?: "Erro ao registrar consulta."
                    _consultationCreationState.value = ConsultationCreationState.Error(message)
                }
            } catch (e: Exception) {
                _consultationCreationState.value = ConsultationCreationState.Error(e.message ?: "Erro inesperado ao criar consulta.")
            }
        }
    }

    fun resetConsultationCreationState() {
        _consultationCreationState.value = ConsultationCreationState.Idle
    }
}

