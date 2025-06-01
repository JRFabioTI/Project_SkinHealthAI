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

// Importações que podem ser necessárias dependendo do seu projeto
import com.example.skinhealthai.viewmodel.ImageUploadViewModel
import com.example.skinhealthai.utils.FileUtils
import com.example.skinhealthai.utils.ImageStorage
import com.example.skinhealthai.ui.theme.screens.HomeScreen
import com.example.skinhealthai.viewmodel.LoginViewModel


// Objeto que define todas as rotas da aplicação
object AppRoutes {
    const val SIGNUP = "signup"
    const val LOGIN = "login"
    const val HOME = "home"
    const val PATIENT_LIST = "patient_list"
    const val ANALYSIS_HISTORY = "analysis_history"

    // Rota base para registro/edição de paciente
    const val PATIENT_REGISTER_BASE = "patient_register"
    // Rota para edição de paciente com ID opcional (agora usando 'val')
    val PATIENT_REGISTER_WITH_ID = "$PATIENT_REGISTER_BASE?patientId={patientId}"

    // Rota da tela de prontuário
    const val PATIENT_RECORD_BASE = "patient_record"
    // Agora 'val' pois contém um placeholder
    val PATIENT_RECORD_WITH_PATIENT_ID = "$PATIENT_RECORD_BASE/{patientId}"
    // Rota para o prontuário que aceita patientId (obrigatório) e consultationId (opcional)
    val PATIENT_RECORD_WITH_PATIENT_ID_AND_CONSULTATION_ID = "$PATIENT_RECORD_WITH_PATIENT_ID?consultationId={consultationId}"


    const val IMAGE_GALLERY = "image_gallery"
    const val SCAN_IMAGE_BASE = "scan_image"
    val SCAN_IMAGE_WITH_INDEX = "$SCAN_IMAGE_BASE/{index}"

    // Rota da tela de consulta, que agora espera o patientId
    const val CONSULTATION_SCREEN_BASE = "consultation_screen"
    val CONSULTATION_SCREEN_WITH_PATIENT_ID = "$CONSULTATION_SCREEN_BASE/{patientId}"
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val imageUploadViewModel: ImageUploadViewModel = viewModel()
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

        // Rota para a Tela de Consulta
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

        // Rota para a Tela de Cadastro/Edição de Paciente
        composable(
            route = AppRoutes.PATIENT_REGISTER_WITH_ID,
            arguments = listOf(navArgument("patientId") {
                type = NavType.IntType
                defaultValue = -1 // Correção: Mantenha o defaultValue para IntType opcional
                // REMOVA nullable = true daqui, pois IntType não permite nulos
            })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getInt("patientId")
            PatientRegisterScreen(
                navController = navController,
                patientId = if (patientId == -1) null else patientId
            )
        }

        // Rota para a Tela de Prontuário
        composable(
            route = AppRoutes.PATIENT_RECORD_WITH_PATIENT_ID_AND_CONSULTATION_ID,
            arguments = listOf(
                navArgument("patientId") {
                    type = NavType.IntType
                    nullable = false // patientId é obrigatório e não pode ser nulo
                },
                navArgument("consultationId") { // Argumento para consultationId
                    type = NavType.IntType
                    defaultValue = -1 // Correção: Forneça um defaultValue para IntType opcional
                    // REMOVA nullable = true daqui, pois IntType não permite nulos
                }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getInt("patientId")
            val consultationId = backStackEntry.arguments?.getInt("consultationId")

            if (patientId != null) {
                PatientRecordScreen(
                    navController = navController,
                    patientId = patientId,
                    consultationId = if (consultationId == -1) null else consultationId
                )
            } else {
                // Esta condição idealmente não deveria ser alcançada com nullable = false para patientId
                Text("Erro: ID do paciente não encontrado para o prontuário.")
            }
        }

        composable(AppRoutes.IMAGE_GALLERY) {
            ImageGalleryScreen(navController = navController)
        }

        // Rota para ImageDetailScreen
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
                        // Lógica de navegação para análise, se aplicável
                    }
                )
            } else {
                Text("Imagem não encontrada para o índice $index")
            }
        }
    }
}