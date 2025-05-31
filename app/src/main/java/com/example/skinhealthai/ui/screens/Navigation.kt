package com.example.skinhealthai.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
// Importe o ViewModel para ImageUpload se ainda for usado em algum lugar (ex: ScanImage)
import com.example.skinhealthai.viewmodel.ImageUploadViewModel
// Importe FileUtils e ImageStorage se ainda usados no ScanImage
import com.example.skinhealthai.utils.FileUtils
import com.example.skinhealthai.utils.ImageStorage

// Importe todas as telas necessárias (remova as redundâncias se já estiverem no mesmo package)
import com.example.skinhealthai.ui.screens.LoginScreen // Certifique-se do caminho correto
import com.example.skinhealthai.ui.screens.ImageGalleryScreen
import com.example.skinhealthai.ui.screens.ImageDetailScreen
import com.example.skinhealthai.ui.screens.PatientListScreen
import com.example.skinhealthai.ui.screens.AnalysisHistoryScreen
import com.example.skinhealthai.ui.screens.PatientRecordScreen
import com.example.skinhealthai.ui.screens.SignUpScreen
import com.example.skinhealthai.ui.screens.ConsultationScreen // <-- Importe ConsultationScreen
import com.example.skinhealthai.ui.screens.PatientRegisterScreen // <-- Importe PatientRegisterScreen
import com.example.skinhealthai.ui.theme.screens.HomeScreen // Verifique se há duplicidade no import de HomeScreen

object AppRoutes {
    const val SIGNUP = "signup"
    const val LOGIN = "login"
    const val HOME = "home"
    const val PATIENT_LIST = "patient_list"
    const val ANALYSIS_HISTORY = "analysis_history"

    // Rota da tela de prontuário, agora com photoUri opcional
    const val PATIENT_RECORD_BASE = "patient_record"
    // Use PATIENT_RECORD_WITH_ID para passar o ID do paciente
    const val PATIENT_RECORD_WITH_ID = "$PATIENT_RECORD_BASE/{patientId}"
    // Adicione um parâmetro de query opcional para a URI da foto
    const val PATIENT_RECORD_WITH_ID_AND_PHOTO = "$PATIENT_RECORD_WITH_ID?photoUri={photoUri}"

    const val IMAGE_GALLERY = "image_gallery"
    const val SCAN_IMAGE_BASE = "scan_image"
    const val SCAN_IMAGE_WITH_INDEX = "$SCAN_IMAGE_BASE/{index}"
    const val PATIENT_REGISTER = "patient_register"

    // Rota da tela de consulta, que agora espera o patientId
    const val CONSULTATION_SCREEN_BASE = "consultation_screen"
    const val CONSULTATION_SCREEN_WITH_PATIENT_ID = "$CONSULTATION_SCREEN_BASE/{patientId}"
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val imageUploadViewModel: ImageUploadViewModel = viewModel() // Mantido se usado em ScanImageDetail

    NavHost(navController = navController, startDestination = AppRoutes.SIGNUP) {

        composable(AppRoutes.SIGNUP) {
            SignUpScreen(navController)
        }
        composable(AppRoutes.LOGIN) {
            LoginScreen(navController)
        }

        composable(AppRoutes.HOME) {
            HomeScreen(
                navController = navController,
                imageViewModel = imageUploadViewModel
            )
        }

        composable(AppRoutes.PATIENT_LIST) {
            PatientListScreen(
                navController = navController,
            )
        }

        // Rota para a Tela de Consulta, agora com patientId como IntType
        composable(
            route = AppRoutes.CONSULTATION_SCREEN_WITH_PATIENT_ID,
            arguments = listOf(navArgument("patientId") { type = NavType.IntType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getInt("patientId")
            ConsultationScreen(
                navController = navController,
                patientId = patientId,
            )
        }

        composable(AppRoutes.ANALYSIS_HISTORY) {
            AnalysisHistoryScreen(navController = navController)
        }

        composable(AppRoutes.PATIENT_REGISTER) {
            PatientRegisterScreen(navController = navController)
        }

        // Rota para a Tela de Prontuário, agora com patientId como IntType e photoUri opcional
        composable(
            route = AppRoutes.PATIENT_RECORD_WITH_ID_AND_PHOTO, // Use a rota que inclui photoUri
            arguments = listOf(
                navArgument("patientId") { type = NavType.IntType }, // patientId como IntType
                navArgument("photoUri") {
                    type = NavType.StringType // photoUri é uma string
                    nullable = true          // pode ser nulo
                    defaultValue = "null"    // valor padrão para evitar null-pointer em alguns casos
                }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getInt("patientId")
            val photoUri = backStackEntry.arguments?.getString("photoUri")

            PatientRecordScreen(
                navController = navController,
                patientId = patientId,
                // imageViewModel removido, pois PatientRecordScreen não o usa mais diretamente
            )
        }

        composable(AppRoutes.IMAGE_GALLERY) {
            ImageGalleryScreen(navController = navController)
        }

        // Rota para ImageDetailScreen (descomentada e corrigida)
//        composable(
//            route = AppRoutes.SCAN_IMAGE_WITH_INDEX,
//            arguments = listOf(navArgument("index") { type = NavType.IntType })
//        ) { backStackEntry ->
//            val index = backStackEntry.arguments?.getInt("index") ?: 0
//            val bitmap = ImageStorage.getImage(index) // Assumindo que ImageStorage.getImage retorna Bitmap?
//
//            if (bitmap != null) {
//                ImageDetailScreen(
//                    bitmap = bitmap,
//                    onAnalyzeClick = {
//                        val file = FileUtils.saveBitmapToFile(context, bitmap)
//                        // Use ImageUploadViewModel apenas se ele for para upload GERAL.
//                        // Se for para upload específico de consulta, o ViewModel da consulta deve lidar.
//                        imageUploadViewModel.uploadImage(file)
//                    }
//                )
//            } else {
//                Text("Imagem não encontrada para o índice $index")
//            }
//        }
    }
}