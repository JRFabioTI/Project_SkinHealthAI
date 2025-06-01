package com.example.skinhealthai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinhealthai.data.model.PatientRequest
import com.example.skinhealthai.data.model.PatientResponse
import com.example.skinhealthai.repository.PatientRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


sealed class PatientUiState {
    object Idle : PatientUiState()
    object Loading : PatientUiState()
    data class Success(val patient: PatientResponse) : PatientUiState()
    data class Error(val message: String) : PatientUiState()
}

sealed class SinglePatientUiState {
    object Idle : SinglePatientUiState()
    object Loading : SinglePatientUiState()
    data class Success(val patient: PatientResponse) : SinglePatientUiState()
    data class Error(val message: String) : SinglePatientUiState()
}

sealed class DeletePatientUiState {
    object Idle : DeletePatientUiState()
    object Loading : DeletePatientUiState()
    object Success : DeletePatientUiState()
    data class Error(val message: String) : DeletePatientUiState()
}


class PatientViewModel(
    private val repository: PatientRepository = PatientRepository()
) : ViewModel() {

    private val _registerState = MutableStateFlow<PatientUiState>(PatientUiState.Idle)
    val registerState: StateFlow<PatientUiState> = _registerState.asStateFlow()

    private val _patientList = MutableStateFlow<List<PatientResponse>>(emptyList())
    val patientList: StateFlow<List<PatientResponse>> = _patientList.asStateFlow()

    private val _deleteState = MutableStateFlow<DeletePatientUiState>(DeletePatientUiState.Idle)
    val deleteState: StateFlow<DeletePatientUiState> = _deleteState.asStateFlow()

    private val _selectedPatient = MutableStateFlow<SinglePatientUiState>(SinglePatientUiState.Idle)
    val selectedPatient: StateFlow<SinglePatientUiState> = _selectedPatient.asStateFlow()

    fun registerPatient(patientRequest: PatientRequest) {
        viewModelScope.launch {
            _registerState.value = PatientUiState.Loading
            try {
                val response = repository.createPatient(patientRequest)
                if (response.isSuccessful && response.body() != null) {
                    _registerState.value = PatientUiState.Success(response.body()!!)
                    fetchPatients()
                } else {
                    val message = response.errorBody()?.string() ?: "Erro desconhecido no servidor"
                    _registerState.value = PatientUiState.Error(message)
                }
            } catch (e: Exception) {
                _registerState.value = PatientUiState.Error(e.message ?: "Erro inesperado")
            }
        }
    }

    fun updatePatient(patientId: Int, patientRequest: PatientRequest) {
        viewModelScope.launch {
            _registerState.value = PatientUiState.Loading
            try {
                val response = repository.updatePatient(patientId, patientRequest)
                if (response.isSuccessful && response.body() != null) {
                    _registerState.value = PatientUiState.Success(response.body()!!)
                    fetchPatients()
                } else {
                    val message = response.errorBody()?.string() ?: "Erro desconhecido ao atualizar paciente"
                    _registerState.value = PatientUiState.Error(message)
                }
            } catch (e: Exception) {
                _registerState.value = PatientUiState.Error(e.message ?: "Erro de rede ao atualizar paciente")
            }
        }
    }

    fun fetchPatients() {
        viewModelScope.launch {
            try {
                val response = repository.getAllPatients()
                if (response.isSuccessful && response.body() != null) {
                    _patientList.value = response.body()!!
                } else {
                    println("Erro ao buscar pacientes: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("Exceção ao buscar pacientes: ${e.message}")
            }
        }
    }

    fun fetchPatientById(patientId: Int) {
        viewModelScope.launch {
            _selectedPatient.value = SinglePatientUiState.Loading
            try {
                val response = repository.getPatientById(patientId)
                if (response.isSuccessful && response.body() != null) {
                    _selectedPatient.value = SinglePatientUiState.Success(response.body()!!)
                } else {
                    val message = response.errorBody()?.string() ?: "Erro ao buscar paciente"
                    _selectedPatient.value = SinglePatientUiState.Error(message)
                }
            } catch (e: Exception) {
                _selectedPatient.value = SinglePatientUiState.Error(e.message ?: "Erro de rede ao buscar paciente")
            }
        }
    }

    fun deletePatient(patientId: Int) {
        viewModelScope.launch {
            _deleteState.value = DeletePatientUiState.Loading
            try {
                val response = repository.deletePatient(patientId)
                if (response.isSuccessful) {
                    _deleteState.value = DeletePatientUiState.Success
                    fetchPatients()
                } else {
                    val message = response.errorBody()?.string() ?: "Erro desconhecido ao excluir paciente"
                    _deleteState.value = DeletePatientUiState.Error(message)
                }
            } catch (e: Exception) {
                _deleteState.value = DeletePatientUiState.Error(e.message ?: "Erro de rede ao excluir paciente")
            }
        }
    }

    fun resetRegisterState() {
        _registerState.value = PatientUiState.Idle
    }

    fun resetDeleteState() {
        _deleteState.value = DeletePatientUiState.Idle
    }

    fun resetSelectedPatientState() {
        _selectedPatient.value = SinglePatientUiState.Idle
    }
}