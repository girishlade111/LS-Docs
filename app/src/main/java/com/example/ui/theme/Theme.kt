package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = HighDensityCanvas,
    primaryContainer = PurpleLightBg,
    onPrimaryContainer = PurpleDark,
    secondary = Purple80,
    background = HighDensityCanvas,
    onBackground = HighDensityTextPrimary,
    surface = HighDensityContainer,
    onSurface = HighDensityTextPrimary,
    surfaceVariant = HighDensityBorder,
    onSurfaceVariant = HighDensityTextSecondary,
    outline = HighDensityBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = PurpleDark,
    primaryContainer = Purple40,
    onPrimaryContainer = PurpleLightBg,
    secondary = DensityTeal,
    background = DarkCanvas,
    onBackground = DarkTextPrimary,
    surface = DarkContainer,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkBorder,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

@Composable
fun LSDocsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
