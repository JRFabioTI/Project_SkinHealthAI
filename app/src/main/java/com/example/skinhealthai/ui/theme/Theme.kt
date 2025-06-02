package com.example.skinhealthai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = White,
    secondary = BlueSecondary,
    onSecondary = White,
    background = LightGray,
    surface = White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = White,
    secondary = BlueSecondary,
    onSecondary = White,
    background = Color.Black,
    surface = Color.DarkGray,
    onBackground = White,
    onSurface = White
)

@Composable
fun SkinHealthAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
