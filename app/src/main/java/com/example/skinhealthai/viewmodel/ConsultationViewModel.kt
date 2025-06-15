package com.example.skinhealthai.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinhealthai.data.model.ConsultationResponse
import com.example.skinhealthai.data.model.ConsultationRequest
import com.example.skinhealthai.data.model.FileImageWithAnalysisResponse
import com.example.skinhealthai.data.model.PatientResponse
import com.example.skinhealthai.data.model.UploadImageResponse
import com.example.skinhealthai.repository.PatientRepository
import com.example.skinhealthai.repository.ConsultationRepository
import com.example.skinhealthai.repository.FileImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException

sealed class PatientDataUiState {
    object Loading : PatientDataUiState()
    data class PatientLoaded(val patient: PatientResponse) : PatientDataUiState()
    data class Error(val message: String) : PatientDataUiState()
    object Idle : PatientDataUiState()
}

sealed class ConsultationCreationState {
    object Idle : ConsultationCreationState()
    object Loading : ConsultationCreationState()
    data class Success(val consultation: ConsultationResponse) : ConsultationCreationState()
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
    // CORREÇÃO: Passar ConsultationResponse no sucesso da atualização
    data class Success(val consultation: ConsultationResponse) : UpdateConsultationUiState()
    data class Error(val message: String) : UpdateConsultationUiState()
}

sealed class DeleteConsultationUiState {
    object Idle : DeleteConsultationUiState()
    object Loading : DeleteConsultationUiState()
    object Success : DeleteConsultationUiState()
    data class Error(val message: String) : DeleteConsultationUiState()
}

sealed class ImageUploadState {
    object Idle : ImageUploadState()
    object Loading : ImageUploadState()
    // NOVO: Passa o objeto UploadImageResponse completo no sucesso
    data class Success(val uploadedFile: UploadImageResponse) : ImageUploadState()
    data class Error(val message: String) : ImageUploadState()
}

class ConsultationViewModel(
    private val patientRepository: PatientRepository = PatientRepository(),
    private val consultationRepository: ConsultationRepository = ConsultationRepository(),
    private val fileImageRepository: FileImageRepository = FileImageRepository()
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

    private val _deleteConsultationState = MutableStateFlow<DeleteConsultationUiState>(DeleteConsultationUiState.Idle)
    val deleteConsultationState: StateFlow<DeleteConsultationUiState> = _deleteConsultationState.asStateFlow()

    private val _imageUploadState = MutableStateFlow<ImageUploadState>(ImageUploadState.Idle)
    val imageUploadState: StateFlow<ImageUploadState> = _imageUploadState.asStateFlow()

    private val _existingImageUrls = MutableStateFlow<List<FileImageWithAnalysisResponse>>(emptyList())
    val existingImageUrls: StateFlow<List<FileImageWithAnalysisResponse>> = _existingImageUrls.asStateFlow()


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
                    // CORREÇÃO: Passar o corpo da resposta para o estado Success
                    _consultationCreationState.value = ConsultationCreationState.Success(response.body()!!)
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
                    val consultation = response.body()!!
                    _singleConsultationUiState.value = SingleConsultationUiState.Loaded(consultation)
                    // NOVO: existingImageUrls agora é populado com imagesWithAnalysis
                    _existingImageUrls.value = consultation.imagesWithAnalysis ?: emptyList()
                } else {
                    val message = response.errorBody()?.string() ?: "Erro ao carregar consulta para edição."
                    _singleConsultationUiState.value = SingleConsultationUiState.Error(message)
                    _existingImageUrls.value = emptyList() // Limpa em caso de erro
                }
            } catch (e: Exception) {
                _singleConsultationUiState.value = SingleConsultationUiState.Error(e.message ?: "Falha na conexão ou erro desconhecido ao carregar consulta.")
                _existingImageUrls.value = emptyList() // Limpa em caso de erro
            }
        }
    }

    fun updateConsultation(consultationId: Int, consultationRequest: ConsultationRequest) {
        viewModelScope.launch {
            _updateConsultationState.value = UpdateConsultationUiState.Loading
            try {
                val response = consultationRepository.updateConsultation(consultationId, consultationRequest)
                if (response.isSuccessful && response.body() != null) {
                    _updateConsultationState.value = UpdateConsultationUiState.Success(response.body()!!)
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

    fun uploadImageForConsultation(bitmap: Bitmap, consultationId: Int) {
        _imageUploadState.value = ImageUploadState.Loading
        viewModelScope.launch {
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val byteArray = stream.toByteArray()
                val requestFile = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val fileName = "image_${System.currentTimeMillis()}.jpg"
                val fileObjPart = MultipartBody.Part.createFormData("file_obj", fileName, requestFile)
                val consultationIdBody = consultationId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val response = fileImageRepository.uploadImageToMinio(fileObjPart, consultationIdBody)

                if (response.isSuccessful && response.body() != null) {
                    val uploadResponse = response.body()!!
                    // NOVO: Passa o UploadImageResponse completo para o estado de sucesso
                    _imageUploadState.value = ImageUploadState.Success(uploadedFile = uploadResponse)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                    _imageUploadState.value = ImageUploadState.Error("Falha ao fazer upload da imagem: ${response.code()} - $errorBody")
                }
            } catch (e: IOException) {
                _imageUploadState.value = ImageUploadState.Error("Erro de conexão ou I/O: ${e.message}")
            } catch (e: Exception) {
                _imageUploadState.value = ImageUploadState.Error("Erro ao processar imagem para upload: ${e.message}")
            }
        }
    }

    fun resetExistingImageUrls() {
        _existingImageUrls.value = emptyList()
    }

    fun resetImageUploadState() {
        _imageUploadState.value = ImageUploadState.Idle
    }
}