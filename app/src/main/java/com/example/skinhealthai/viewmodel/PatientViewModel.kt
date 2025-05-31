package com.example.skinhealthai.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinhealthai.data.model.PatientRequest
import com.example.skinhealthai.data.model.PatientResponse
import com.example.skinhealthai.repository.PatientRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow

sealed class PatientUiState {
    object Idle : PatientUiState()
    object Loading : PatientUiState()
    data class Success(val patient: PatientResponse) : PatientUiState()
    data class Error(val message: String) : PatientUiState()
}

class PatientViewModel(
    private val repository: PatientRepository = PatientRepository()
) : ViewModel() {

    private val _registerState = MutableStateFlow<PatientUiState>(PatientUiState.Idle)
    val registerState: StateFlow<PatientUiState> = _registerState

    private val _patientList = MutableStateFlow<List<PatientResponse>>(emptyList())
    val patientList: StateFlow<List<PatientResponse>> = _patientList

    fun registerPatient(patientRequest: PatientRequest) {
        viewModelScope.launch {
            _registerState.value = PatientUiState.Loading
            try {
                val response = repository.createPatient(patientRequest)
                if (response.isSuccessful && response.body() != null) {
                    _registerState.value = PatientUiState.Success(response.body()!!)
                    fetchPatients() // Atualiza a lista após cadastro
                } else {
                    val message = response.errorBody()?.string() ?: "Erro desconhecido no servidor"
                    _registerState.value = PatientUiState.Error(message)
                }
            } catch (e: Exception) {
                _registerState.value = PatientUiState.Error(e.message ?: "Erro inesperado")
            }
        }
    }

    fun fetchPatients() {
        viewModelScope.launch {
            try {
                val response = repository.getAllPatients()
                if (response.isSuccessful && response.body() != null) {
                    _patientList.value = response.body()!!
                }
            } catch (e: Exception) {
                // Pode logar erro ou atualizar estado se quiser
            }
        }
    }

    fun resetRegisterState() {
        _registerState.value = PatientUiState.Idle
    }
}