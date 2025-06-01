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
import com.example.skinhealthai.viewmodel.ImageUploadViewModel
import com.example.skinhealthai.utils.FileUtils
import com.example.skinhealthai.utils.ImageStorage
import com.example.skinhealthai.ui.theme.screens.HomeScreen
import com.example.skinhealthai.viewmodel.LoginViewModel
import androidx.navigation.compose.rememberNavController // Certifique-se de ter este import se usar rememberNavController aqui

// Objeto que define todas as rotas da aplicação
object AppRoutes {
    const val SIGNUP = "signup"
    const val LOGIN = "login"
    const val HOME = "home"
    const val PATIENT_LIST = "patient_list"
    const val ANALYSIS_HISTORY = "analysis_history"

    // Rota base para registro/edição de paciente
    const val PATIENT_REGISTER_BASE = "patient_register"
    // Rota para edição de paciente com ID opcional (sem nullable=true no NavArgument para IntType)
    const val PATIENT_REGISTER_WITH_ID = "$PATIENT_REGISTER_BASE?patientId={patientId}"

    // Rota da tela de prontuário, agora com photoUri opcional
    const val PATIENT_RECORD_BASE = "patient_record"
    const val PATIENT_RECORD_WITH_ID = "$PATIENT_RECORD_BASE/{patientId}"
    const val PATIENT_RECORD_WITH_ID_AND_PHOTO = "$PATIENT_RECORD_WITH_ID?photoUri={photoUri}"

    const val IMAGE_GALLERY = "image_gallery"
    const val SCAN_IMAGE_BASE = "scan_image"
    const val SCAN_IMAGE_WITH_INDEX = "$SCAN_IMAGE_BASE/{index}"

    // Rota da tela de consulta, que agora espera o patientId
    const val CONSULTATION_SCREEN_BASE = "consultation_screen"
    const val CONSULTATION_SCREEN_WITH_PATIENT_ID = "$CONSULTATION_SCREEN_BASE/{patientId}"
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) { // Adicione rememberNavController como default
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
            route = AppRoutes.PATIENT_REGISTER_WITH_ID, // Use a rota exata com o parâmetro de query
            arguments = listOf(navArgument("patientId") {
                type = NavType.IntType
                defaultValue = -1 // Valor padrão para indicar "nenhum ID"
                // IMPORTANTE: Não adicione 'nullable = true' para NavType.IntType aqui
            })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getInt("patientId")
            // Se o ID for -1 (valor padrão), passe null para a tela. Caso contrário, passe o ID.
            PatientRegisterScreen(
                navController = navController,
                patientId = if (patientId == -1) null else patientId
            )
        }

        // Rota para a Tela de Prontuário
        composable(
            route = AppRoutes.PATIENT_RECORD_WITH_ID_AND_PHOTO,
            arguments = listOf(
                navArgument("patientId") {
                    type = NavType.IntType
                    defaultValue = -1 // Adicione defaultValue para patientId
                    // IMPORTANTE: Não adicione 'nullable = true' para NavType.IntType aqui
                },
                navArgument("photoUri") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = "null"
                }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getInt("patientId")
            val photoUri = backStackEntry.arguments?.getString("photoUri")

            PatientRecordScreen(
                navController = navController,
                patientId = if (patientId == -1) null else patientId,
            )
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
                        // Exemplo de navegação para PatientRecordScreen para análise:
                        // Você pode passar o patientId real aqui se souber, ou um ID temporário.
                        // navController.navigate("${AppRoutes.PATIENT_RECORD_BASE}/0?photoUri=${file.absolutePath}")
                    }
                )
            } else {
                Text("Imagem não encontrada para o índice $index")
            }
        }
    }
}