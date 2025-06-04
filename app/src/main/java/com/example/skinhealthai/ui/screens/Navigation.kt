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
import androidx.navigation.compose.rememberNavController
import com.example.skinhealthai.utils.FileUtils
import com.example.skinhealthai.utils.ImageStorage
import com.example.skinhealthai.ui.theme.screens.HomeScreen
import com.example.skinhealthai.viewmodel.LoginViewModel


object AppRoutes {
    const val SIGNUP = "signup"
    const val LOGIN = "login"
    const val HOME = "home"
    const val PATIENT_LIST = "patient_list"
    const val PATIENT_HISTORY = "patient_history"
    const val PATIENT_REGISTER = "patient_register"
    val PATIENT_REGISTER_WITH_ID = "$PATIENT_REGISTER?patientId={patientId}"
    const val PATIENT_RECORD_BASE = "patient_record"
    val PATIENT_RECORD_WITH_PATIENT_ID = "$PATIENT_RECORD_BASE/{patientId}"
    const val IMAGE_GALLERY = "image_gallery"
    const val SCAN_IMAGE_BASE = "scan_image"
    val SCAN_IMAGE_WITH_INDEX = "$SCAN_IMAGE_BASE/{index}"
    const val CONSULTATION_SCREEN_BASE = "consultation_screen"
    val CONSULTATION_SCREEN_WITH_PATIENT_ID = "$CONSULTATION_SCREEN_BASE/{patientId}"
    val CONSULTATION_SCREEN_EDIT = "$CONSULTATION_SCREEN_BASE/{patientId}?consultationId={consultationId}"
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val loginViewModel: LoginViewModel = viewModel()

    NavHost(navController = navController, startDestination = AppRoutes.SIGNUP) {

        composable(AppRoutes.SIGNUP) {
            SignUpScreen(navController)
        }
        composable(AppRoutes.LOGIN) {
            LoginScreen(navController, loginViewModel = loginViewModel)
        }

        composable(AppRoutes.HOME) {
            HomeScreen(
                navController = navController,
                loginViewModel = loginViewModel
            )
        }

        composable(AppRoutes.PATIENT_LIST) {
            PatientListScreen(
                navController = navController,
            )
        }

        composable(
            route = AppRoutes.CONSULTATION_SCREEN_WITH_PATIENT_ID,
            arguments = listOf(navArgument("patientId") { type = NavType.IntType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getInt("patientId")
            ConsultationScreen(
                navController = navController,
                patientId = patientId,
                consultationId = null
            )
        }

        composable(
            route = AppRoutes.CONSULTATION_SCREEN_EDIT,
            arguments = listOf(
                navArgument("patientId") { type = NavType.IntType },
                navArgument("consultationId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getInt("patientId")
            val consultationId = backStackEntry.arguments?.getInt("consultationId")

            ConsultationScreen(
                navController = navController,
                patientId = patientId,
                consultationId = if (consultationId == -1) null else consultationId
            )
        }

        composable(AppRoutes.PATIENT_HISTORY) {
            PatientHistoryScreen(
                navController = navController,
            )
        }

        //rota edição de paciente
        composable(
            route = AppRoutes.PATIENT_REGISTER_WITH_ID,
            arguments = listOf(navArgument("patientId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getInt("patientId")
            PatientRegisterScreen(
                navController = navController,
                patientId = if (patientId == -1) null else patientId
            )
        }

        //rota cadastro paciente
        composable(AppRoutes.PATIENT_REGISTER) {
            PatientRegisterScreen(
                navController = navController,
                patientId = null
            )
        }

        composable(
            route = AppRoutes.PATIENT_RECORD_WITH_PATIENT_ID,
            arguments = listOf(
                navArgument("patientId") {
                    type = NavType.IntType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getInt("patientId")

            if (patientId != null) {
                PatientRecordScreen(
                    navController = navController,
                    patientId = patientId,
                )
            } else {
                Text("Erro: ID do paciente não encontrado para o prontuário.")
            }
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
                    }
                )
            } else {
                Text("Imagem não encontrada para o índice $index")
            }
        }
    }
}