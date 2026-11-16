package com.example.spot.ui.motion

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** false when the system's animations are off; provided once in SpotRoot. */
val LocalAnimationsEnabled = staticCompositionLocalOf { true }

/** App-wide press feedback for frequently used screens: small and fast. */
object PressMotion {
    const val Scale = 0.97f      // chips, buttons
    const val CardScale = 0.98f  // large surfaces scale less
    const val DurationMs = 100
    const val ColorMs = 120      // chip selection color
    val EaseOut = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
}

/**
 * Scale that dips while [interactionSource] is pressed. Read `.value` inside a
 * graphicsLayer lambda so the screen does not recompose every frame.
 */
@Composable
fun rememberPressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = PressMotion.Scale,
): State<Float> {
    val pressed by interactionSource.collectIsPressedAsState()
    val animate = LocalAnimationsEnabled.current
    return animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = if (animate) tween(PressMotion.DurationMs, easing = PressMotion.EaseOut) else snap(),
        label = "pressScale",
    )
}

@Composable
fun quickColorSpec(): AnimationSpec<Color> =
    if (LocalAnimationsEnabled.current) tween(PressMotion.ColorMs, easing = PressMotion.EaseOut) else snap()
