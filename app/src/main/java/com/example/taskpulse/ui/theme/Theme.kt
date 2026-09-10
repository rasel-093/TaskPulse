package com.example.taskpulse.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = IndigoDim,
    onPrimaryContainer = Color(0xFF1B243B),
    secondary = Ochre,
    onSecondary = Color.White,
    secondaryContainer = OchreDim,
    onSecondaryContainer = Color(0xFF382900),
    tertiary = Moss,
    onTertiary = Color.White,
    tertiaryContainer = MossDim,
    onTertiaryContainer = Color(0xFF182E15),
    background = Fog,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperShade,
    onSurfaceVariant = Graphite,
    outline = Rule,
    outlineVariant = Color(0xFFBBB6A8)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkIndigo,
    onPrimary = Color(0xFF10192D),
    primaryContainer = DarkIndigoDim,
    onPrimaryContainer = Color(0xFFD6E0FA),
    secondary = DarkOchre,
    onSecondary = Color(0xFF3B2A06),
    secondaryContainer = DarkOchreDim,
    onSecondaryContainer = Color(0xFFFFE0A8),
    tertiary = DarkMoss,
    onTertiary = Color(0xFF122810),
    tertiaryContainer = DarkMossDim,
    onTertiaryContainer = Color(0xFFD3E6D0),
    background = DarkFog,
    onBackground = DarkInk,
    surface = DarkPaper,
    onSurface = DarkInk,
    surfaceVariant = DarkPaperShade,
    onSurfaceVariant = DarkGraphite,
    outline = DarkRule,
    outlineVariant = Color(0xFF505563)
)

object TaskPulseTheme {
    val colors: TaskPulseColorPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalTaskPulseColors.current
}

@Composable
fun TaskPulseTheme(
    // Default to the new Fog & Paper editorial palette
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val pulsePalette = if (darkTheme) DarkTaskPulsePalette else LightTaskPulsePalette

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalTaskPulseColors provides pulsePalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = TaskPulseShapes,
            content = content
        )
    }
}