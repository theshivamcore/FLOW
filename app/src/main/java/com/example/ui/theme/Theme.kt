package com.example.ui.theme

import android.app.Activity
import android.os.Build
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
    primary = FlowTeal,
    onPrimary = DarkBackground,
    primaryContainer = FlowTealDark,
    onPrimaryContainer = FlowTealGlow,
    secondary = FlowEmerald,
    onSecondary = DarkBackground,
    tertiary = FlowIndigo,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextHigh,
    surface = DarkSurface,
    onSurface = TextHigh,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextMedium,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = FlowTealDark,
    onPrimary = LightSurface,
    primaryContainer = FlowTeal,
    onPrimaryContainer = DarkBackground,
    secondary = FlowEmerald,
    onSecondary = LightSurface,
    tertiary = FlowIndigo,
    onTertiary = LightSurface,
    background = LightBackground,
    onBackground = TextHighLight,
    surface = LightSurface,
    onSurface = TextHighLight,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = TextMediumLight,
    outline = LightBorder
)

@Composable
fun FlowTheme(
    darkTheme: Boolean = true, // Default to sleek obsidian dark theme
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
