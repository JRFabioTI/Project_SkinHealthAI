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
import kotlinx.coroutines.flow.asStateFlow // Importe este para asStateFlow
import kotlinx.coroutines.launch

// Estados para a UI da tela de consulta (carregamento do paciente)
sealed class PatientDataUiState {
    object Loading : PatientDataUiState()
    data class PatientLoaded(val patient: PatientResponse) : PatientDataUiState()
    data class Error(val message: String) : PatientDataUiState()
    object Idle : PatientDataUiState() // Adicione o estado Idle aqui também
}

// Estados para a operação de criação de consulta
sealed class ConsultationCreationState {
    object Idle : ConsultationCreationState()
    object Loading : ConsultationCreationState()
    object Success : ConsultationCreationState()
    data class Error(val message: String) : ConsultationCreationState()
}

// Estados para o carregamento do histórico de consultas de um paciente
sealed class PatientConsultationsUiState {
    object Loading : PatientConsultationsUiState()
    data class Loaded(val consultations: List<ConsultationResponse>) : PatientConsultationsUiState()
    data class Error(val message: String) : PatientConsultationsUiState()
    object Idle : PatientConsultationsUiState()
}

// NOVO: Estados para a UI de uma ÚNICA consulta (para edição)
sealed class SingleConsultationUiState {
    object Idle : SingleConsultationUiState()
    object Loading : SingleConsultationUiState()
    data class Loaded(val consultation: ConsultationResponse) : SingleConsultationUiState()
    data class Error(val message: String) : SingleConsultationUiState()
}

// NOVO: Estados para a operação de ATUALIZAÇÃO de consulta
sealed class UpdateConsultationUiState {
    object Idle : UpdateConsultationUiState()
    object Loading : UpdateConsultationUiState()
    object Success : UpdateConsultationUiState()
    data class Error(val message: String) : UpdateConsultationUiState()
}


class ConsultationViewModel(
    private val patientRepository: PatientRepository = PatientRepository(),
    private val consultationRepository: ConsultationRepository = ConsultationRepository()
) : ViewModel() {

    private val _patientDataUiState = MutableStateFlow<PatientDataUiState>(PatientDataUiState.Idle) // Alterado para Idle como start
    val patientDataUiState: StateFlow<PatientDataUiState> = _patientDataUiState.asStateFlow() // Use asStateFlow

    private val _consultationCreationState = MutableStateFlow<ConsultationCreationState>(ConsultationCreationState.Idle)
    val consultationCreationState: StateFlow<ConsultationCreationState> = _consultationCreationState.asStateFlow() // Use asStateFlow

    private val _patientConsultationsUiState = MutableStateFlow<PatientConsultationsUiState>(PatientConsultationsUiState.Idle)
    val patientConsultationsUiState: StateFlow<PatientConsultationsUiState> = _patientConsultationsUiState.asStateFlow() // Use asStateFlow

    // NOVO: StateFlow para a consulta que está sendo editada/visualizada individualmente
    private val _singleConsultationUiState = MutableStateFlow<SingleConsultationUiState>(SingleConsultationUiState.Idle)
    val singleConsultationUiState: StateFlow<SingleConsultationUiState> = _singleConsultationUiState.asStateFlow()

    // NOVO: StateFlow para o estado da operação de atualização da consulta
    private val _updateConsultationState = MutableStateFlow<UpdateConsultationUiState>(UpdateConsultationUiState.Idle)
    val updateConsultationState: StateFlow<UpdateConsultationUiState> = _updateConsultationState.asStateFlow()


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
                    loadPatientConsultations(consultationRequest.patientId) // Use consultationRequest.patient
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

    // NOVO: Função para carregar uma consulta específica para edição
    fun loadSingleConsultation(consultationId: Int) {
        viewModelScope.launch {
            _singleConsultationUiState.value = SingleConsultationUiState.Loading
            try {
                val response = consultationRepository.getConsultationById(consultationId) // <-- CORREÇÃO AQUI // Usando getConsultation por ID
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

    // NOVO: Função para atualizar uma consulta
    fun updateConsultation(consultationId: Int, consultationRequest: ConsultationRequest) {
        viewModelScope.launch {
            _updateConsultationState.value = UpdateConsultationUiState.Loading
            try {
                val response = consultationRepository.updateConsultation(consultationId, consultationRequest)
                if (response.isSuccessful) { // updateConsultation pode retornar Response<ConsultationResponse> ou Response<Unit>
                    _updateConsultationState.value = UpdateConsultationUiState.Success
                    // Opcional: Recarregar a lista de consultas do paciente se necessário
                    // ou apenas resetar o estado e o usuário voltará para a tela anterior
                } else {
                    val message = response.errorBody()?.string() ?: "Erro desconhecido ao atualizar consulta."
                    _updateConsultationState.value = UpdateConsultationUiState.Error(message)
                }
            } catch (e: Exception) {
                _updateConsultationState.value = UpdateConsultationUiState.Error(e.message ?: "Falha na conexão ao atualizar consulta.")
            }
        }
    }

    fun resetConsultationCreationState() {
        _consultationCreationState.value = ConsultationCreationState.Idle
    }

    // NOVO: Função para resetar o estado da consulta individual
    fun resetSingleConsultationState() {
        _singleConsultationUiState.value = SingleConsultationUiState.Idle
    }

    // NOVO: Função para resetar o estado de atualização
    fun resetUpdateConsultationState() {
        _updateConsultationState.value = UpdateConsultationUiState.Idle
    }
}