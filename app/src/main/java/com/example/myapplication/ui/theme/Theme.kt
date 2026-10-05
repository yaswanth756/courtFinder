package com.example.myapplication.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CourtGreenLight,
    onPrimary = DarkSlateBackground,
    primaryContainer = CourtGreenDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = CourtLime,
    onSecondary = DarkSlateBackground,
    background = DarkSlateBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSlateSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSlateCard,
    onSurfaceVariant = TextSecondaryDark,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = CourtGreen,
    onPrimary = LightSurface,
    primaryContainer = CourtGreenLight,
    onPrimaryContainer = DarkSlateBackground,
    secondary = CourtGreenDark,
    onSecondary = LightSurface,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightCard,
    onSurfaceVariant = TextSecondaryLight,
    error = ErrorRed
)

@Composable
fun CourtFinderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
