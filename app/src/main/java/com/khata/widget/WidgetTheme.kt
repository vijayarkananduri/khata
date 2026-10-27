package com.khata.widget

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.toArgb
import com.khata.R

internal data class WidgetPalette(
    val isDark: Boolean,
    val cardBackground: Int,
    val activePeriodBackground: Int,
    val idlePeriodBackground: Int,
    val title: Int,
    val primaryText: Int,
    val secondaryText: Int,
    val activePeriodText: Int,
    val idlePeriodText: Int,
    val ringTrack: Int,
    val ringProgress: Int
)

internal fun resolveWidgetDarkMode(themeMode: String?, systemDark: Boolean): Boolean = when (themeMode?.uppercase()) {
    "LIGHT" -> false
    "DARK" -> true
    else -> systemDark
}

internal object WidgetTheme {
    private const val PREFS = "khata_widget_prefs"

    fun resolve(context: Context): WidgetPalette {
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val mode = preferences.getString("theme_mode", "SYSTEM") ?: "SYSTEM"
        val systemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val dark = resolveWidgetDarkMode(mode, systemDark)
        val wallpaperMode = mode.equals("WALLPAPER", ignoreCase = true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val wallpaperScheme = if (wallpaperMode) {
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else null

        val accent = wallpaperScheme?.primary?.toArgb() ?: if (dark) 0xFFF08B6F.toInt() else 0xFF9E3D27.toInt()
        val activeContainer = wallpaperScheme?.primaryContainer?.toArgb() ?: if (dark) 0xFF633425.toInt() else 0xFFF5DCD2.toInt()
        val onActiveContainer = wallpaperScheme?.onPrimaryContainer?.toArgb() ?: if (dark) 0xFFFFD9C9.toInt() else 0xFF49251A.toInt()

        return WidgetPalette(
            isDark = dark,
            cardBackground = if (dark) R.drawable.widget_card_bg_dark else R.drawable.widget_card_bg,
            activePeriodBackground = if (dark) R.drawable.widget_period_active_dark else R.drawable.widget_period_active,
            idlePeriodBackground = if (dark) R.drawable.widget_period_idle_dark else R.drawable.widget_period_idle,
            title = accent,
            primaryText = if (dark) 0xFFF4EFE6.toInt() else 0xFF292521.toInt(),
            secondaryText = if (dark) 0xFFC0B8AE.toInt() else 0xFF625A52.toInt(),
            activePeriodText = onActiveContainer,
            idlePeriodText = if (dark) 0xFFD1C8BE.toInt() else 0xFF625A52.toInt(),
            ringTrack = if (dark) 0xFF4A4036.toInt() else 0xFFE9E0D4.toInt(),
            ringProgress = accent
        )
    }
}
