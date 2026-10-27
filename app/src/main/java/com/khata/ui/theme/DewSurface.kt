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
    val glowAlpha = if (dark) 0.045f else 0f
    Box(
        Modifier.fillMaxSize()
            .background(background)
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
        targetValue = if (onClick != null && pressed) 0.975f else 1f,
        animationSpec = if (reduceMotion) snap() else spring(dampingRatio = .72f, stiffness = 520f),
        label = "dew-press"
    )
    val dark = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < .5f
    val outline = androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = if (dark) .82f else .92f)
    // A solid, palette-aware surface avoids light-theme banding and dynamic-color haze while
    // retaining the soft lift and pressed scale without a fragile runtime blur graph.
    val solidTone = tone.copy(alpha = 1f)
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(elevation, shape, clip = false)
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ) else Modifier
            )
            .background(solidTone, shape)
            .border(BorderStroke(1.dp, outline), shape)
    ) {
        content()
    }
}
