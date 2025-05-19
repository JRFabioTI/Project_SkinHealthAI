package com.example.skinhealthai.ui.theme.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.skinhealthai.viewmodel.ImageUploadViewModel

@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val imageUploadViewModel: ImageUploadViewModel = viewModel()


    NavHost(navController = navController, startDestination = "signup") {
        composable("signup") { SignUpScreen(navController) }
        composable("login") { LoginScreen(navController) }
//        composable("home") {
//            HomeScreen(
//                navController = navController,
//                imageViewModel = imageUploadViewModel
//            )
//        }
        composable("home") {
            HomeScreen2(
                navController = navController,
                imageViewModel = imageUploadViewModel
            )
        }

        composable("patient_record") {
            PatientRecordScreen(
                userName = "Dr. Médico",
                onLogout = { /* logout */ },
                onBack = { navController.popBackStack() },  // Aqui volta para a HomeScreen
                imageViewModel = imageUploadViewModel
            )
        }








//        composable("image_gallery") {
//            ImageGalleryScreen(navController = navController)
//        }
//        composable(
//            "scan_image/{index}",
//            arguments = listOf(navArgument("index") { type = NavType.IntType })
//        ) { backStackEntry ->
//            val index = backStackEntry.arguments?.getInt("index") ?: 0
//            val bitmap = ImageStorage.getImage(index)
//
//            if (bitmap != null) {
//                ImageDetailScreen(
//                    bitmap = bitmap,
//                    onAnalyzeClick = {
//                        val file = FileUtils.saveBitmapToFile(context, bitmap)
//                        imageUploadViewModel.uploadImage(file)
//                    }
//                )
//            } else {
//                Text("Imagem não encontrada")
//            }
//        }
    }
}
