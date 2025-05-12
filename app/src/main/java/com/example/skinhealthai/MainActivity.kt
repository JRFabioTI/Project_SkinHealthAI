package com.example.skinhealthai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.skinhealthai.screens.AppNavigation
import com.example.skinhealthai.ui.theme.SkinHealthAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkinHealthAITheme {
                // Configuração da navegação
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}
