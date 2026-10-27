package com.iditechs.filmu.android.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

private val FilmuColors = darkColorScheme(
    primary = Color(0xFF3AACF0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1A5276),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFE8C547),
    onSecondary = Color.Black,
    background = Color(0xFF0A1628),
    onBackground = Color.White,
    surface = Color(0xFF0F1E36),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF162A4A),
    onSurfaceVariant = Color(0xFFB0C4DE),
    outline = Color(0xFF2A4A6B),
    error = Color(0xFFCF6679),
    onError = Color.Black,
)

private val FilmuTypography = Typography()

@Composable
fun FilmuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FilmuColors,
        typography = FilmuTypography,
        content = content,
    )
}
