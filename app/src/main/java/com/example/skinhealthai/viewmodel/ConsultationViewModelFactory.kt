package com.example.skinhealthai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.skinhealthai.data.network.ApiService
import com.example.skinhealthai.repository.PatientRepository
import com.example.skinhealthai.repository.ConsultationRepository
import com.example.skinhealthai.repository.FileImageRepository
import com.example.skinhealthai.ui.viewmodel.ConsultationViewModel

class ConsultationViewModelFactory(
    private val apiService: ApiService,
    private val patientRepository: PatientRepository,
    private val consultationRepository: ConsultationRepository,
    private val fileImageRepository: FileImageRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConsultationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConsultationViewModel(patientRepository, consultationRepository, fileImageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}