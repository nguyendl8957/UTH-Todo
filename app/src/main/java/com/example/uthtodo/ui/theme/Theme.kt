package com.example.uthtodo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.graphics.Color

@Composable
fun UTHTodoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    accentColor: Color = Color(0xFF2F5BEA),
    content: @Composable () -> Unit
) {
    // Dynamic Material You deliberately disabled: professor's four explicit accents win.
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            background = Color(0xFF121216),
            onBackground = Color(0xFFF5F5F7),
            surface = Color(0xFF222226),
            onSurface = Color(0xFFF5F5F7),
            onSurfaceVariant = Color(0xFFBABAC0),
            outline = Color(0xFF53535A)
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            background = Color(0xFFF5F5F7),
            onBackground = Color(0xFF1C1C1E),
            surface = Color.White,
            onSurface = Color(0xFF1C1C1E),
            onSurfaceVariant = Color(0xFF6B6B70),
            outline = Color(0xFFD8D8DE)
        )
    }
    val baseDensity = LocalDensity.current
    MaterialTheme(colorScheme = scheme, typography = Typography) {
        CompositionLocalProvider(
            LocalDensity provides Density(baseDensity.density, baseDensity.fontScale * 1.12f),
            content = content
        )
    }
}
