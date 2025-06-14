package com.example.skinhealthai.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinhealthai.data.model.ConsultationResponse
import com.example.skinhealthai.data.model.ConsultationRequest
import com.example.skinhealthai.data.model.PatientResponse
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
    // CORREÇÃO: Passar ConsultationResponse no sucesso para obter o ID da nova consulta
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

// Novo: Estado para o upload da imagem
sealed class ImageUploadState {
    object Idle : ImageUploadState()
    object Loading : ImageUploadState()
    data class Success(val message: String, val consultationId: String) : ImageUploadState()
    data class Error(val message: String) : ImageUploadState()
}

class ConsultationViewModel(
    private val patientRepository: PatientRepository = PatientRepository(),
    private val consultationRepository: ConsultationRepository = ConsultationRepository(),
    private val fileImageRepository: FileImageRepository = FileImageRepository() // Injete o repositório de imagem
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

    // NOVO: Estado para o upload da imagem
    private val _imageUploadState = MutableStateFlow<ImageUploadState>(ImageUploadState.Idle)
    val imageUploadState: StateFlow<ImageUploadState> = _imageUploadState.asStateFlow()


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
                if (response.isSuccessful && response.body() != null) {
                    // CORREÇÃO: Passar o corpo da resposta para o estado Success
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

    // NOVO: Função para fazer o upload da imagem
    fun uploadImageForConsultation(bitmap: Bitmap, consultationId: Int) {
        _imageUploadState.value = ImageUploadState.Loading
        viewModelScope.launch {
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream) // 90% de qualidade
                val byteArray = stream.toByteArray()

                val requestFile = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
                // O nome do arquivo a ser enviado. Use um nome único.
                val fileName = "image_${System.currentTimeMillis()}.jpg"
                val fileObjPart = MultipartBody.Part.createFormData("file_obj", fileName, requestFile)

                // Cria RequestBody para o 'consultation_id'
                val consultationIdBody = consultationId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                val response = fileImageRepository.uploadImageToMinio(fileObjPart, consultationIdBody)

                if (response.isSuccessful && response.body() != null) {
                    val uploadResponse = response.body()!!
                    _imageUploadState.value = ImageUploadState.Success(uploadResponse.message, uploadResponse.consultationId)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                    _imageUploadState.value = ImageUploadState.Error("Falha ao fazer upload da imagem: ${response.code()} - $errorBody")
                }
            } catch (e: IOException) {
                // Erros de rede ou IO
                _imageUploadState.value = ImageUploadState.Error("Erro de conexão ou I/O: ${e.message}")
            } catch (e: Exception) {
                // Outros erros
                _imageUploadState.value = ImageUploadState.Error("Erro ao processar imagem para upload: ${e.message}")
            }
        }
    }

    // NOVO: Resetar o estado de upload da imagem
    fun resetImageUploadState() {
        _imageUploadState.value = ImageUploadState.Idle
    }
}