package com.khata.widget

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetThemeTest {
    @Test fun explicitLightModeOverridesDarkSystemTheme() {
        assertFalse(resolveWidgetDarkMode("LIGHT", systemDark = true))
    }

    @Test fun explicitDarkModeOverridesLightSystemTheme() {
        assertTrue(resolveWidgetDarkMode("DARK", systemDark = false))
    }

    @Test fun systemAndWallpaperModesFollowSystemAppearance() {
        assertFalse(resolveWidgetDarkMode("SYSTEM", systemDark = false))
        assertTrue(resolveWidgetDarkMode("SYSTEM", systemDark = true))
        assertFalse(resolveWidgetDarkMode("WALLPAPER", systemDark = false))
        assertTrue(resolveWidgetDarkMode("WALLPAPER", systemDark = true))
    }
}
