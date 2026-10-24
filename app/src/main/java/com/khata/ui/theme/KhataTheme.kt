package com.khata.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.khata.R

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

val GoogleSansFamily = FontFamily(
    Font(R.font.google_sans_400, FontWeight.Normal),
    Font(R.font.google_sans_500, FontWeight.Medium),
    Font(R.font.google_sans_600, FontWeight.SemiBold),
    Font(R.font.google_sans_700, FontWeight.Bold)
)
val CaveatFamily = FontFamily(Font(R.font.caveat_variable, FontWeight.Normal), Font(R.font.caveat_variable, FontWeight.Medium))
val LocalDewSurface = staticCompositionLocalOf { false }

private val BaseTypography = Typography()
private val KhataTypography = BaseTypography.copy(
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = GoogleSansFamily),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = GoogleSansFamily),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = GoogleSansFamily),
    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = GoogleSansFamily),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = GoogleSansFamily),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = GoogleSansFamily),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = GoogleSansFamily),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = GoogleSansFamily),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = GoogleSansFamily),
    bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = GoogleSansFamily),
    bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = GoogleSansFamily),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = GoogleSansFamily),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = GoogleSansFamily),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = GoogleSansFamily),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = GoogleSansFamily)
)

@Composable
fun KhataTheme(themeMode: String = "SYSTEM", dewSurface: Boolean = false, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }
    val base = if (dark) darkColorScheme(
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
    val colors = if (dewSurface) base.copy(
        surface = base.surface.copy(alpha = 0f),
        surfaceVariant = base.surfaceVariant.copy(alpha = 0f)
    ) else base
    CompositionLocalProvider(LocalDewSurface provides dewSurface) {
        MaterialTheme(colorScheme = colors, typography = KhataTypography, content = content)
    }
}
