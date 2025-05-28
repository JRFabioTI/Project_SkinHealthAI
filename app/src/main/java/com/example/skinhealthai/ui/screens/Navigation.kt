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
import com.example.skinhealthai.utils.FileUtils
import com.example.skinhealthai.utils.ImageStorage
import com.example.skinhealthai.viewmodel.ImageUploadViewModel

// Importe todas as telas necessárias:
import com.example.skinhealthai.ui.screens.LoginScreen
import com.example.skinhealthai.ui.theme.screens.HomeScreen
import com.example.skinhealthai.ui.screens.ImageGalleryScreen
import com.example.skinhealthai.ui.screens.ImageDetailScreen
import com.example.skinhealthai.ui.screens.PatientListScreen
import com.example.skinhealthai.ui.screens.AnalysisHistoryScreen
import com.example.skinhealthai.ui.screens.PatientRecordScreen
import com.example.skinhealthai.ui.screens.SignUpScreen

object AppRoutes {
    const val SIGNUP = "signup"
    const val LOGIN = "login"
    const val HOME = "home"
    const val PATIENT_LIST = "patient_list"
    const val ANALYSIS_HISTORY = "analysis_history"
    const val PATIENT_RECORD_BASE = "patient_record"
    const val PATIENT_RECORD_WITH_ID = "$PATIENT_RECORD_BASE/{patientId}"
    const val IMAGE_GALLERY = "image_gallery"
    const val SCAN_IMAGE_BASE = "scan_image"
    const val SCAN_IMAGE_WITH_INDEX = "$SCAN_IMAGE_BASE/{index}"
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val imageUploadViewModel: ImageUploadViewModel = viewModel()

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
            PatientListScreen(navController = navController)
        }

        composable(AppRoutes.ANALYSIS_HISTORY) {
            AnalysisHistoryScreen(navController = navController)
        }

        composable(
            route = AppRoutes.PATIENT_RECORD_WITH_ID,
            arguments = listOf(navArgument("patientId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val patientIdString = backStackEntry.arguments?.getString("patientId")
            val patientId = patientIdString?.toIntOrNull()

            PatientRecordScreen(
                navController = navController,
                patientId = patientId,
                imageViewModel = imageUploadViewModel
            )
        }

        composable(AppRoutes.IMAGE_GALLERY) {
            ImageGalleryScreen(navController = navController)
        }

        composable(
            route = AppRoutes.SCAN_IMAGE_WITH_INDEX,
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            val bitmap = ImageStorage.getImage(index)

            if (bitmap != null) {
                ImageDetailScreen(
                    bitmap = bitmap,
                    onAnalyzeClick = {
                        val file = FileUtils.saveBitmapToFile(context, bitmap)
                        imageUploadViewModel.uploadImage(file)
                    }
                )
            } else {
                Text("Imagem não encontrada para o índice $index")
            }
        }
    }
}