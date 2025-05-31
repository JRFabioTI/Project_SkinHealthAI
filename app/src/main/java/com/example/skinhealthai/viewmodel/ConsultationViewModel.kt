// com.example.skinhealthai.ui.viewmodel/ConsultationViewModel.kt
package com.example.skinhealthai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Importe ConsultationResponse, já que seus repositórios o retornam
import com.example.skinhealthai.data.model.ConsultationResponse // Ajustado para ConsultationResponse
import com.example.skinhealthai.data.model.ConsultationRequest
import com.example.skinhealthai.data.model.PatientResponse
import com.example.skinhealthai.repository.PatientRepository
import com.example.skinhealthai.repository.ConsultationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Estados para a UI da tela de consulta (carregamento do paciente)
sealed class PatientDataUiState {
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

// Estados para o carregamento do histórico de consultas
sealed class PatientConsultationsUiState {
    object Loading : PatientConsultationsUiState()
    // Ajustado para List<ConsultationResponse> para ser consistente com o repositório
    data class Loaded(val consultations: List<ConsultationResponse>) : PatientConsultationsUiState()
    data class Error(val message: String) : PatientConsultationsUiState()
    object Idle : PatientConsultationsUiState()
}


class ConsultationViewModel(
    private val patientRepository: PatientRepository = PatientRepository(),
    private val consultationRepository: ConsultationRepository = ConsultationRepository()
) : ViewModel() {

    private val _patientDataUiState = MutableStateFlow<PatientDataUiState>(PatientDataUiState.Loading)
    val patientDataUiState: StateFlow<PatientDataUiState> = _patientDataUiState

    private val _consultationCreationState = MutableStateFlow<ConsultationCreationState>(ConsultationCreationState.Idle)
    val consultationCreationState: StateFlow<ConsultationCreationState> = _consultationCreationState

    private val _patientConsultationsUiState = MutableStateFlow<PatientConsultationsUiState>(PatientConsultationsUiState.Idle)
    val patientConsultationsUiState: StateFlow<PatientConsultationsUiState> = _patientConsultationsUiState


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
                    // Se a consulta for criada com sucesso, recarregue as consultas do paciente
                    consultationRequest.patientId?.let { loadPatientConsultations(it) }
                } else {
                    val message = response.errorBody()?.string() ?: "Erro ao registrar consulta."
                    _consultationCreationState.value = ConsultationCreationState.Error(message)
                }
            } catch (e: Exception) {
                _consultationCreationState.value = ConsultationCreationState.Error(e.message ?: "Erro inesperado ao criar consulta.")
            }
        }
    }

    fun loadPatientConsultations(patientId: Int) {
        viewModelScope.launch {
            _patientConsultationsUiState.value = PatientConsultationsUiState.Loading
            try {
                val response = consultationRepository.getConsultationsByPatientId(patientId)
                if (response.isSuccessful && response.body() != null) {
                    // Use ConsultationResponse aqui para ser consistente com o retorno do repositório
                    _patientConsultationsUiState.value = PatientConsultationsUiState.Loaded(response.body()!!)
                } else {
                    val message = response.errorBody()?.string() ?: "Nenhuma consulta encontrada ou erro ao carregar."
                    _patientConsultationsUiState.value = PatientConsultationsUiState.Error(message)
                }
            } catch (e: Exception) {
                _patientConsultationsUiState.value = PatientConsultationsUiState.Error(e.message ?: "Erro ao carregar histórico de consultas.")
            }
        }
    }

    fun resetConsultationCreationState() {
        _consultationCreationState.value = ConsultationCreationState.Idle
    }
}