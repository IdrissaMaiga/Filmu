package com.iditechs.filmu.android.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

private val FilmuColors = darkColorScheme(
    primary = Color(0xFFE50914),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF8B0000),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFB0B0B0),
    onSecondary = Color.Black,
    background = Color(0xFF0A0A0A),
    onBackground = Color.White,
    surface = Color(0xFF141414),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1F1F1F),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF333333),
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
