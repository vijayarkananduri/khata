package com.khata.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Parchment = Color(0xFFF7F2EA)
val LightSurface = Color(0xFFFFFCF6)
val LightAlt = Color(0xFFF0E9DC)
val LightInk = Color(0xFF1A2E31)
val LightMuted = Color(0xFF6B7A7C)
val LightBorder = Color(0xFFE5DED2)
val Ember = Color(0xFFD6663A)
val EmberDeep = Color(0xFFB4402E)
val Tide = Color(0xFF4E7C7B)
val Saffron = Color(0xFFE4B04A)
val Plum = Color(0xFF8B5A5A)
val Brick = Color(0xFFB4402E)
val DarkBackground = Color(0xFF142527)
val DarkSurface = Color(0xFF1D3134)
val DarkAlt = Color(0xFF24393C)
val DarkInk = Color(0xFFE8E2D6)
val DarkMuted = Color(0xFF8FA3A5)
val DarkBorder = Color(0xFF2A4042)
val DarkEmber = Color(0xFFEF8659)
val DarkTide = Color(0xFF7BA8A7)
val DarkSaffron = Color(0xFFE9C069)
val DarkBrick = Color(0xFFD65E5E)

@Composable
fun KhataTheme(themeMode: String = "SYSTEM", content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }
    val colors = if (dark) darkColorScheme(
        primary = DarkEmber, onPrimary = Color(0xFF291308), secondary = DarkTide,
        tertiary = DarkSaffron, error = DarkBrick, background = DarkBackground,
        surface = DarkSurface, onSurface = DarkInk, onBackground = DarkInk,
        surfaceVariant = DarkAlt, onSurfaceVariant = DarkMuted, outline = DarkBorder
    ) else lightColorScheme(
        primary = Ember, onPrimary = Color.White, secondary = Tide,
        tertiary = Saffron, error = Brick, background = Parchment,
        surface = LightSurface, onSurface = LightInk, onBackground = LightInk,
        surfaceVariant = LightAlt, onSurfaceVariant = LightMuted, outline = LightBorder
    )
    MaterialTheme(colorScheme = colors, typography = androidx.compose.material3.Typography(), content = content)
}
