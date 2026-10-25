package com.khata

import androidx.compose.ui.graphics.Color
import com.khata.ui.theme.DarkBackground
import com.khata.ui.theme.DarkEmber
import com.khata.ui.theme.DarkInk
import com.khata.ui.theme.DarkMuted
import com.khata.ui.theme.DarkSurface
import com.khata.ui.theme.EmberDeep
import com.khata.ui.theme.LightInk
import com.khata.ui.theme.LightMuted
import com.khata.ui.theme.LightSurface
import com.khata.ui.theme.Parchment
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeContrastTest {
    @Test fun fixedPaletteTextAndBrandAccentsMeetWcagAaForNormalText() {
        listOf(
            LightInk to LightSurface,
            LightMuted to Parchment,
            EmberDeep to LightSurface,
            DarkInk to DarkSurface,
            DarkMuted to DarkBackground,
            DarkEmber to DarkSurface
        ).forEach { (foreground, background) ->
            assertTrue("Contrast ${ratio(foreground, background)} is below 4.5", ratio(foreground, background) >= 4.5)
        }
    }

    private fun ratio(foreground: Color, background: Color): Double {
        val a = luminance(foreground)
        val b = luminance(background)
        return (maxOf(a, b) + .05) / (minOf(a, b) + .05)
    }

    private fun luminance(color: Color): Double {
        fun linear(component: Float): Double {
            val value = component.toDouble()
            return if (value <= .04045) value / 12.92 else Math.pow((value + .055) / 1.055, 2.4)
        }
        return .2126 * linear(color.red) + .7152 * linear(color.green) + .0722 * linear(color.blue)
    }
}
