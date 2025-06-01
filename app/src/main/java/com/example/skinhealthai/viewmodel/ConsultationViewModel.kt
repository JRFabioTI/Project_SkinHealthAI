package com.example.skinhealthai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinhealthai.data.model.ConsultationResponse
import com.example.skinhealthai.data.model.ConsultationRequest
import com.example.skinhealthai.data.model.PatientResponse
import com.example.skinhealthai.repository.PatientRepository
import com.example.skinhealthai.repository.ConsultationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PatientDataUiState {
    object Loading : PatientDataUiState()
    data class PatientLoaded(val patient: PatientResponse) : PatientDataUiState()
    data class Error(val message: String) : PatientDataUiState()
    object Idle : PatientDataUiState()
}

sealed class ConsultationCreationState {
    object Idle : ConsultationCreationState()
    object Loading : ConsultationCreationState()
    object Success : ConsultationCreationState()
    data class Error(val message: String) : ConsultationCreationState()
}

sealed class PatientConsultationsUiState {
    object Loading : PatientConsultationsUiState()
    data class Loaded(val consultations: List<ConsultationResponse>) : PatientConsultationsUiState()
    data class Error(val message: String) : PatientConsultationsUiState()
    object Idle : PatientConsultationsUiState()
}

sealed class SingleConsultationUiState {
    object Idle : SingleConsultationUiState()
    object Loading : SingleConsultationUiState()
    data class Loaded(val consultation: ConsultationResponse) : SingleConsultationUiState()
    data class Error(val message: String) : SingleConsultationUiState()
}

sealed class UpdateConsultationUiState {
    object Idle : UpdateConsultationUiState()
    object Loading : UpdateConsultationUiState()
    object Success : UpdateConsultationUiState()
    data class Error(val message: String) : UpdateConsultationUiState()
}

sealed class DeleteConsultationUiState {
    object Idle : DeleteConsultationUiState()
    object Loading : DeleteConsultationUiState()
    object Success : DeleteConsultationUiState()
    data class Error(val message: String) : DeleteConsultationUiState()
}

class ConsultationViewModel(
    private val patientRepository: PatientRepository = PatientRepository(),
    private val consultationRepository: ConsultationRepository = ConsultationRepository()
) : ViewModel() {

    private val _patientDataUiState = MutableStateFlow<PatientDataUiState>(PatientDataUiState.Idle)
    val patientDataUiState: StateFlow<PatientDataUiState> = _patientDataUiState.asStateFlow()

    private val _consultationCreationState = MutableStateFlow<ConsultationCreationState>(ConsultationCreationState.Idle)
    val consultationCreationState: StateFlow<ConsultationCreationState> = _consultationCreationState.asStateFlow()

    private val _patientConsultationsUiState = MutableStateFlow<PatientConsultationsUiState>(PatientConsultationsUiState.Idle)
    val patientConsultationsUiState: StateFlow<PatientConsultationsUiState> = _patientConsultationsUiState.asStateFlow()

    private val _singleConsultationUiState = MutableStateFlow<SingleConsultationUiState>(SingleConsultationUiState.Idle)
    val singleConsultationUiState: StateFlow<SingleConsultationUiState> = _singleConsultationUiState.asStateFlow()

    private val _updateConsultationState = MutableStateFlow<UpdateConsultationUiState>(UpdateConsultationUiState.Idle)
    val updateConsultationState: StateFlow<UpdateConsultationUiState> = _updateConsultationState.asStateFlow()

    // --- NOVO: Estado para exclusão de consulta ---
    private val _deleteConsultationState = MutableStateFlow<DeleteConsultationUiState>(DeleteConsultationUiState.Idle)
    val deleteConsultationState: StateFlow<DeleteConsultationUiState> = _deleteConsultationState.asStateFlow()
    // --- FIM NOVO ---


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
                    loadPatientConsultations(consultationRequest.patientId)
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

    fun loadSingleConsultation(consultationId: Int) {
        viewModelScope.launch {
            _singleConsultationUiState.value = SingleConsultationUiState.Loading
            try {
                val response = consultationRepository.getConsultationById(consultationId)
                if (response.isSuccessful && response.body() != null) {
                    _singleConsultationUiState.value = SingleConsultationUiState.Loaded(response.body()!!)
                } else {
                    val message = response.errorBody()?.string() ?: "Erro ao carregar consulta para edição."
                    _singleConsultationUiState.value = SingleConsultationUiState.Error(message)
                }
            } catch (e: Exception) {
                _singleConsultationUiState.value = SingleConsultationUiState.Error(e.message ?: "Falha na conexão ou erro desconhecido ao carregar consulta.")
            }
        }
    }

    fun updateConsultation(consultationId: Int, consultationRequest: ConsultationRequest) {
        viewModelScope.launch {
            _updateConsultationState.value = UpdateConsultationUiState.Loading
            try {
                val response = consultationRepository.updateConsultation(consultationId, consultationRequest)
                if (response.isSuccessful) {
                    _updateConsultationState.value = UpdateConsultationUiState.Success
                } else {
                    val message = response.errorBody()?.string() ?: "Erro desconhecido ao atualizar consulta."
                    _updateConsultationState.value = UpdateConsultationUiState.Error(message)
                }
            } catch (e: Exception) {
                _updateConsultationState.value = UpdateConsultationUiState.Error(e.message ?: "Falha na conexão ao atualizar consulta.")
            }
        }
    }

    fun deleteConsultation(consultationId: Int) {
        viewModelScope.launch {
            _deleteConsultationState.value = DeleteConsultationUiState.Loading
            try {
                val response = consultationRepository.deleteConsultation(consultationId)
                if (response.isSuccessful) {
                    _deleteConsultationState.value = DeleteConsultationUiState.Success
                } else {
                    val message = response.errorBody()?.string() ?: "Erro desconhecido ao excluir consulta."
                    _deleteConsultationState.value = DeleteConsultationUiState.Error(message)
                }
            } catch (e: Exception) {
                _deleteConsultationState.value = DeleteConsultationUiState.Error(e.message ?: "Falha na conexão ao excluir consulta.")
            }
        }
    }

    fun resetConsultationCreationState() {
        _consultationCreationState.value = ConsultationCreationState.Idle
    }

    fun resetSingleConsultationState() {
        _singleConsultationUiState.value = SingleConsultationUiState.Idle
    }

    fun resetUpdateConsultationState() {
        _updateConsultationState.value = UpdateConsultationUiState.Idle
    }

    fun resetDeleteConsultationState() {
        _deleteConsultationState.value = DeleteConsultationUiState.Idle
    }
}