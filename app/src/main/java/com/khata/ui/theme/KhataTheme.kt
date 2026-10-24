package com.khata.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.khata.R

val Parchment = Color(0xFFF5F3EF)
val LightSurface = Color(0xFFFFFFFF)
val LightAlt = Color(0xFFECEFEB)
val LightInk = Color(0xFF182321)
val LightMuted = Color(0xFF66716D)
val LightBorder = Color(0xFFDCE2DD)
val Ember = Color(0xFFC95736)
val EmberDeep = Color(0xFFAA4028)
val Tide = Color(0xFF3E7770)
val Saffron = Color(0xFFD49A2E)
val Plum = Color(0xFF8B5A5A)
val Brick = Color(0xFFB4402E)
val DarkBackground = Color(0xFF11161A)
val DarkSurface = Color(0xFF1B2228)
val DarkAlt = Color(0xFF252E35)
val DarkInk = Color(0xFFF0F2EF)
val DarkMuted = Color(0xFFABB6B1)
val DarkBorder = Color(0xFF37424A)
val DarkEmber = Color(0xFFFF9875)
val DarkTide = Color(0xFF8AC7BD)
val DarkSaffron = Color(0xFFF0C773)
val DarkBrick = Color(0xFFD65E5E)

val GoogleSansFamily = FontFamily(
    Font(R.font.google_sans_400, FontWeight.Normal),
    Font(R.font.google_sans_500, FontWeight.Medium),
    Font(R.font.google_sans_600, FontWeight.SemiBold),
    Font(R.font.google_sans_700, FontWeight.Bold)
)
val CaveatFamily = FontFamily(
    Font(R.font.caveat_variable, FontWeight.Normal),
    Font(R.font.caveat_variable, FontWeight.Medium),
    Font(R.font.caveat_variable, FontWeight.Bold)
)

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

private val LightScheme = lightColorScheme(
    primary = Ember, onPrimary = Color.White, secondary = Tide,
    tertiary = Saffron, error = Brick, background = Parchment,
    surface = LightSurface, onSurface = LightInk, onBackground = LightInk,
    surfaceVariant = LightAlt, onSurfaceVariant = LightMuted, outline = LightBorder
)
private val DarkScheme = darkColorScheme(
    primary = DarkEmber, onPrimary = Color(0xFF291308), secondary = DarkTide,
    tertiary = DarkSaffron, error = DarkBrick, background = DarkBackground,
    surface = DarkSurface, onSurface = DarkInk, onBackground = DarkInk,
    surfaceVariant = DarkAlt, onSurfaceVariant = DarkMuted, outline = DarkBorder
)

@Composable
fun KhataTheme(themeMode: String = "SYSTEM", content: @Composable () -> Unit) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemDark
    }
    val colors = if (themeMode == "WALLPAPER" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (dark) DarkScheme else LightScheme
    MaterialTheme(colorScheme = colors, typography = KhataTypography, content = content)
}
