package com.example.skinhealthai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.skinhealthai.ui.theme.SkinHealthAITheme
import com.example.skinhealthai.ui.theme.screens.AppNavigation

import com.example.skinhealthai.ui.theme.screens.HomeScreen2
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

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
//                    HomeScreen2()
//                }

            }
        }
    }
}
