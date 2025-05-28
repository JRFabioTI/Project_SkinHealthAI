package com.example.skinhealthai.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinhealthai.data.model.Patient
import com.example.skinhealthai.repository.PatientRepository
import kotlinx.coroutines.launch

class PatientViewModel(private val repository: PatientRepository): ViewModel() {

    val patients = MutableLiveData<List<Patient>>()
    val patient = MutableLiveData<Patient>()
    val errorMessage = MutableLiveData<String>()
    val operationSuccess = MutableLiveData<Boolean>()

    fun fetchPatients() {
        viewModelScope.launch {
            try {
                val response = repository.getAllPatients()
                if (response.isSuccessful) {
                    patients.postValue(response.body())
                    operationSuccess.postValue(true)
                } else {
                    errorMessage.postValue("Erro ao carregar pacientes: ${response.message()}")
                    operationSuccess.postValue(false)
                }
            } catch (e: Exception) {
                errorMessage.postValue("Falha na rede: ${e.message}")
                operationSuccess.postValue(false)
            }
        }
    }

    fun fetchPatientById(id: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getPatientById(id)
                if (response.isSuccessful) {
                    patient.postValue(response.body())
                    operationSuccess.postValue(true)
                } else {
                    errorMessage.postValue("Erro ao carregar paciente: ${response.message()}")
                    operationSuccess.postValue(false)
                }
            } catch (e: Exception) {
                errorMessage.postValue("Falha na rede: ${e.message}")
                operationSuccess.postValue(false)
            }
        }
    }

    fun createPatient(patient: Patient) {
        viewModelScope.launch {
            try {
                val response = repository.createPatient(patient)
                if (response.isSuccessful) {
                    operationSuccess.postValue(true)
                    fetchPatients() // atualizar lista
                } else {
                    errorMessage.postValue("Erro ao criar paciente: ${response.message()}")
                    operationSuccess.postValue(false)
                }
            } catch (e: Exception) {
                errorMessage.postValue("Falha na rede: ${e.message}")
                operationSuccess.postValue(false)
            }
        }
    }

    fun updatePatient(id: Int, patient: Patient) {
        viewModelScope.launch {
            try {
                val response = repository.updatePatient(id, patient)
                if (response.isSuccessful) {
                    operationSuccess.postValue(true)
                    fetchPatients()
                } else {
                    errorMessage.postValue("Erro ao atualizar paciente: ${response.message()}")
                    operationSuccess.postValue(false)
                }
            } catch (e: Exception) {
                errorMessage.postValue("Falha na rede: ${e.message}")
                operationSuccess.postValue(false)
            }
        }
    }

    fun deletePatient(id: Int) {
        viewModelScope.launch {
            try {
                val response = repository.deletePatient(id)
                if (response.isSuccessful) {
                    operationSuccess.postValue(true)
                    fetchPatients()
                } else {
                    errorMessage.postValue("Erro ao deletar paciente: ${response.message()}")
                    operationSuccess.postValue(false)
                }
            } catch (e: Exception) {
                errorMessage.postValue("Falha na rede: ${e.message}")
                operationSuccess.postValue(false)
            }
        }
    }
}
