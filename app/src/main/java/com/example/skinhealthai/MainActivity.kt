package com.example.skinhealthai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.skinhealthai.screens.HomeScreen
import com.example.skinhealthai.ui.theme.SkinHealthAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkinHealthAITheme {
                HomeScreen()
            }
        }
    }
}
