package com.example.skinhealthai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.skinhealthai.ui.theme.SkinHealthAITheme
import com.example.skinhealthai.ui.screens.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkinHealthAITheme {
                 //Configuração da navegação
                val navController = rememberNavController()
                AppNavigation(navController = navController)

//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    HomeScreen()
//                }

            }
        }
    }
}
