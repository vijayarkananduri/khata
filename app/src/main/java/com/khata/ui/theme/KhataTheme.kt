package com.khata.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khata.R

// Khata's light palette is intentionally fixed: warm paper, warm card white, ink, and Ember.
val Parchment = Color(0xFFFAF7F0)
val LightSurface = Color(0xFFFFFDF8)
val LightAlt = Color(0xFFF3EEE5)
val LightInk = Color(0xFF292521)
val LightMuted = Color(0xFF746E68)
val LightBorder = Color(0xFFE8E0D5)
val Ember = Color(0xFFC95736)
val EmberDeep = Color(0xFF9E3D27)

// Supporting hues have fixed semantic roles: income/positive, budget attention, and errors.
val Tide = Color(0xFF486B57)
val Saffron = Color(0xFF9A6818)
val Plum = Color(0xFF765A72)
val Brick = Color(0xFFB54137)

// Dark mode is the same brand in a warm charcoal treatment, not a separate palette.
val DarkBackground = Color(0xFF191715)
val DarkSurface = Color(0xFF24211D)
val DarkAlt = Color(0xFF302B25)
val DarkInk = Color(0xFFF4EFE6)
val DarkMuted = Color(0xFFB7ADA0)
val DarkBorder = Color(0xFF4A4036)
val DarkEmber = Color(0xFFF08B6F)
val DarkTide = Color(0xFF8CC5A0)
val DarkSaffron = Color(0xFFE9C476)
val DarkBrick = Color(0xFFFF9B8E)

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
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = GoogleSansFamily, fontSize = 40.sp, lineHeight = 48.sp),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = GoogleSansFamily, fontSize = 36.sp, lineHeight = 44.sp),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = GoogleSansFamily, fontSize = 32.sp, lineHeight = 40.sp),
    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = GoogleSansFamily, fontSize = 30.sp, lineHeight = 38.sp),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = GoogleSansFamily, fontSize = 26.sp, lineHeight = 34.sp),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = GoogleSansFamily, fontSize = 23.sp, lineHeight = 30.sp),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = GoogleSansFamily, fontSize = 21.sp, lineHeight = 28.sp),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = GoogleSansFamily, fontSize = 18.sp, lineHeight = 24.sp),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = GoogleSansFamily, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = GoogleSansFamily, fontSize = 17.sp, lineHeight = 25.sp),
    bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = GoogleSansFamily, fontSize = 16.sp, lineHeight = 23.sp),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = GoogleSansFamily, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = GoogleSansFamily, fontSize = 15.sp, lineHeight = 21.sp),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = GoogleSansFamily, fontSize = 13.sp, lineHeight = 18.sp),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = GoogleSansFamily, fontSize = 13.sp, lineHeight = 18.sp)
)

private val KhataShapes = Shapes(
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp)
)

private val LightScheme = lightColorScheme(
    primary = Ember,
    onPrimary = Color(0xFFFFF8F3),
    primaryContainer = Color(0xFFF5DCD2),
    onPrimaryContainer = Color(0xFF49251A),
    secondary = Tide,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3ECE3),
    onSecondaryContainer = Color(0xFF203629),
    tertiary = Saffron,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF2E6C9),
    onTertiaryContainer = Color(0xFF382A12),
    error = Brick,
    onError = Color.White,
    errorContainer = Color(0xFFF7DEDA),
    onErrorContainer = Color(0xFF511E19),
    background = Parchment,
    onBackground = LightInk,
    surface = LightSurface,
    onSurface = LightInk,
    surfaceVariant = LightAlt,
    onSurfaceVariant = LightMuted,
    outline = LightBorder,
    outlineVariant = Color(0xFFDCD2C5),
    surfaceTint = Ember
)

private val DarkScheme = darkColorScheme(
    primary = DarkEmber,
    onPrimary = Color(0xFF35150D),
    primaryContainer = Color(0xFF633425),
    onPrimaryContainer = Color(0xFFFFD9C9),
    secondary = DarkTide,
    onSecondary = Color(0xFF153322),
    secondaryContainer = Color(0xFF293D30),
    onSecondaryContainer = Color(0xFFD0E9D5),
    tertiary = DarkSaffron,
    onTertiary = Color(0xFF39290A),
    tertiaryContainer = Color(0xFF493716),
    onTertiaryContainer = Color(0xFFF4E0B0),
    error = DarkBrick,
    onError = Color(0xFF3D1110),
    errorContainer = Color(0xFF572724),
    onErrorContainer = Color(0xFFFFDAD4),
    background = DarkBackground,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkAlt,
    onSurfaceVariant = DarkMuted,
    outline = DarkBorder,
    outlineVariant = Color(0xFF594E42),
    surfaceTint = DarkEmber
)

@Composable
fun KhataTheme(themeMode: String = "SYSTEM", content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) DarkScheme else LightScheme,
        typography = KhataTypography,
        shapes = KhataShapes,
        content = content
    )
}
