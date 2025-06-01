package com.example.skinhealthai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinhealthai.data.model.ConsultationResponse
import com.example.skinhealthai.repository.ConsultationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow // Importar asStateFlow
import kotlinx.coroutines.launch

// Estados para a UI da tela de histórico de análises
sealed class AnalysisHistoryUiState {
    object Loading : AnalysisHistoryUiState()
    data class Loaded(val consultations: List<ConsultationResponse>) : AnalysisHistoryUiState()
    data class Error(val message: String) : AnalysisHistoryUiState()
    object Idle : AnalysisHistoryUiState()
}

// NOVO: Estados para a UI da exclusão de consulta
sealed class DeleteConsultationUiState {
    object Idle : DeleteConsultationUiState()
    object Loading : DeleteConsultationUiState()
    object Success : DeleteConsultationUiState()
    data class Error(val message: String) : DeleteConsultationUiState()
}


class AnalysisHistoryViewModel(
    private val consultationRepository: ConsultationRepository = ConsultationRepository()
) : ViewModel() {

    private val _analysisHistoryState = MutableStateFlow<AnalysisHistoryUiState>(AnalysisHistoryUiState.Idle)
    val analysisHistoryState: StateFlow<AnalysisHistoryUiState> = _analysisHistoryState.asStateFlow() // Use asStateFlow

    // NOVO: StateFlow para o estado de exclusão
    private val _deleteConsultationState = MutableStateFlow<DeleteConsultationUiState>(DeleteConsultationUiState.Idle)
    val deleteConsultationState: StateFlow<DeleteConsultationUiState> = _deleteConsultationState.asStateFlow() // Use asStateFlow

    init {
        loadAnalysisHistory()
    }

    fun loadAnalysisHistory() {
        viewModelScope.launch {
            _analysisHistoryState.value = AnalysisHistoryUiState.Loading
            try {
                val response = consultationRepository.getAllConsultations()
                if (response.isSuccessful && response.body() != null) {
                    _analysisHistoryState.value = AnalysisHistoryUiState.Loaded(response.body()!!)
                } else {
                    val message = response.errorBody()?.string() ?: "Erro ao carregar histórico de análises."
                    _analysisHistoryState.value = AnalysisHistoryUiState.Error(message)
                }
            } catch (e: Exception) {
                _analysisHistoryState.value = AnalysisHistoryUiState.Error(e.message ?: "Falha na conexão ou erro desconhecido.")
            }
        }
    }

    // NOVO: Função para deletar uma consulta
    fun deleteConsultation(consultationId: Int) {
        viewModelScope.launch {
            _deleteConsultationState.value = DeleteConsultationUiState.Loading
            try {
                val response = consultationRepository.deleteConsultation(consultationId)
                if (response.isSuccessful) {
                    _deleteConsultationState.value = DeleteConsultationUiState.Success
                    loadAnalysisHistory() // Recarrega a lista após exclusão bem-sucedida
                } else {
                    val message = response.errorBody()?.string() ?: "Erro desconhecido ao excluir consulta."
                    _deleteConsultationState.value = DeleteConsultationUiState.Error(message)
                }
            } catch (e: Exception) {
                _deleteConsultationState.value = DeleteConsultationUiState.Error(e.message ?: "Falha na conexão ao excluir consulta.")
            }
        }
    }

    // NOVO: Função para resetar o estado de exclusão
    fun resetDeleteConsultationState() {
        _deleteConsultationState.value = DeleteConsultationUiState.Idle
    }
}