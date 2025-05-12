package com.example.skinhealthai.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.skinhealthai.viewmodel.ImageViewModel

@Composable
fun AppNavigation(navController: NavHostController) {
    // Criando o ViewModel dentro do NavHost
    val imageViewModel: ImageViewModel = viewModel()

    NavHost(navController = navController, startDestination = "signup") { // Agora começa na tela de cadastro
        composable("signup") { SignUpScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("home") {
            HomeScreen(
                navController = navController,
                imageViewModel = imageViewModel
            )
        }
        composable("image_gallery") {
            ImageGalleryScreen(navController = navController, images = imageViewModel.images.value)
        }
    }
}
