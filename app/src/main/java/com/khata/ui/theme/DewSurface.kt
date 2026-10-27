package com.khata.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DewBackdrop() {
    val background = androidx.compose.material3.MaterialTheme.colorScheme.background
    val dark = background.luminance() < .5f
    val accent = androidx.compose.material3.MaterialTheme.colorScheme.primary
    val glowAlpha = if (dark) 0.055f else 0f
    Box(
        Modifier.fillMaxSize().background(background)
    ) {
        Box(
            Modifier.align(Alignment.TopEnd)
                .fillMaxWidth(.72f)
                .height(270.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = glowAlpha), accent.copy(alpha = glowAlpha * .35f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            Modifier.align(Alignment.BottomStart)
                .fillMaxWidth(.60f)
                .height(240.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = glowAlpha * .65f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
    }
}

/**
 * Khata's Dew surface: a translucent-looking, droplet-gloss material built from safe Compose
 * gradients, a reflective rim, and a soft lift. It deliberately avoids backdrop render graphs,
 * which caused a confirmed GPU renderer crash on some devices.
 */
@Composable
fun DewSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    @Suppress("UNUSED_PARAMETER") zIndex: Float = 10f,
    tone: Color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
    elevation: Dp = 14.dp,
    reduceMotion: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (onClick != null && pressed) .972f else 1f,
        animationSpec = if (reduceMotion) snap() else spring(dampingRatio = .68f, stiffness = 430f),
        label = "dew-press"
    )
    val dark = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < .5f
    val scheme = androidx.compose.material3.MaterialTheme.colorScheme
    val base = tone.copy(alpha = if (dark) .91f else .84f)
    val topSheen = if (dark) Color(0xFF51473D).copy(alpha = .78f) else Color(0xFFFFFEFC).copy(alpha = .86f)
    val lowerDepth = if (dark) Color(0xFF211F1C).copy(alpha = .95f) else Color(0xFFEDE4D9).copy(alpha = .78f)
    val rimBrush = Brush.linearGradient(
        if (dark) listOf(Color.White.copy(alpha = .46f), scheme.outline.copy(alpha = .52f), scheme.primary.copy(alpha = .40f), Color.White.copy(alpha = .18f))
        else listOf(Color.White.copy(alpha = .98f), scheme.outline.copy(alpha = .52f), scheme.primary.copy(alpha = .20f), Color.White.copy(alpha = .78f))
    )
    val specularBrush = Brush.horizontalGradient(
        if (dark) listOf(Color.Transparent, Color.White.copy(alpha = .32f), Color.White.copy(alpha = .11f), Color.Transparent)
        else listOf(Color.Transparent, Color.White.copy(alpha = .94f), Color.White.copy(alpha = .36f), Color.Transparent)
    )
    val lowerRimBrush = Brush.horizontalGradient(
        if (dark) listOf(Color.Transparent, scheme.primary.copy(alpha = .28f), Color.Transparent)
        else listOf(Color.Transparent, Color.White.copy(alpha = .76f), scheme.primary.copy(alpha = .12f), Color.Transparent)
    )

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(elevation + 2.dp, shape, clip = false)
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ) else Modifier
            )
            .background(Brush.verticalGradient(listOf(topSheen, base, lowerDepth)), shape)
            .border(BorderStroke(1.15.dp, rimBrush), shape)
    ) {
        // A soft, curved reflection and thin lower glint give the surface its liquid-droplet read.
        Box(
            Modifier.align(Alignment.TopCenter)
                .fillMaxWidth(.78f)
                .height(3.dp)
                .padding(horizontal = 22.dp)
                .clip(CircleShape)
                .background(specularBrush)
        )
        Box(
            Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth(.54f)
                .height(1.5.dp)
                .padding(horizontal = 28.dp)
                .clip(CircleShape)
                .background(lowerRimBrush)
        )
        content()
    }
}
