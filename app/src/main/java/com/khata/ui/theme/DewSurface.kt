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
import androidx.compose.runtime.staticCompositionLocalOf
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.chrisbanes.haze.materials.CupertinoMaterials

val LocalKhataHazeState = staticCompositionLocalOf<HazeState> {
    error("KhataHazeState must be provided by KhataRoot")
}

@Composable
fun rememberKhataHazeState(): HazeState = rememberHazeState()

@Composable
fun DewBackdrop(hazeState: HazeState) {
    val background = androidx.compose.material3.MaterialTheme.colorScheme.background
    val dark = background.luminance() < .5f
    val glowAlpha = if (dark) 0.10f else 0.11f
    Box(
        Modifier.fillMaxSize()
            .background(background)
            .hazeSource(state = hazeState, zIndex = 0f)
    ) {
        Box(
            Modifier.align(Alignment.TopEnd)
                .fillMaxWidth(.72f)
                .height(270.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Ember.copy(alpha = glowAlpha), Ember.copy(alpha = glowAlpha * .35f), Color.Transparent)
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
                        colors = listOf(Ember.copy(alpha = glowAlpha * .7f), Color.Transparent)
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
    zIndex: Float = 10f,
    tone: Color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
    elevation: Dp = 14.dp,
    reduceMotion: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = LocalKhataHazeState.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (onClick != null && pressed) 0.975f else 1f,
        animationSpec = if (reduceMotion) snap() else spring(dampingRatio = .72f, stiffness = 520f),
        label = "dew-press"
    )
    val dark = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < .5f
    val topRim = if (dark) Color.White.copy(alpha = .28f) else Color.White.copy(alpha = .94f)
    val lowerRim = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = if (dark) .34f else .22f)
    val tint = tone.copy(alpha = .42f)
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
            .hazeSource(state = hazeState, zIndex = zIndex)
            .hazeEffect(state = hazeState, style = CupertinoMaterials.regular(containerColor = tone))
            .background(
                Brush.verticalGradient(
                    listOf(topRim.copy(alpha = .28f), tint, tone.copy(alpha = .22f))
                ),
                shape
            )
            .border(
                BorderStroke(1.dp, Brush.verticalGradient(listOf(topRim, lowerRim, lowerRim.copy(alpha = .10f)))),
                shape
            )
    ) {
        Box(
            Modifier.align(Alignment.TopCenter)
                .fillMaxWidth(.72f)
                .height(1.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, topRim, Color.Transparent)))
        )
        content()
    }
}
