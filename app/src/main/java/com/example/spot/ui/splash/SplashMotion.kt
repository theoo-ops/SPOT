package com.example.spot.ui.splash

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Every duration, delay and easing of the splash. Tune the feel here.
 * Timeline: everything settles at ~1080 ms, then the 250 ms exit, ~1.33 s total.
 */
object SplashMotion {
    /** Set to 3f–5f to watch the choreography in slow motion while tuning. Ship with 1f. */
    const val SlowMotion = 1f

    // Curves (strong ease-out for entrances; ease-in-out for the sparkle settle)
    val EaseOut = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
    val EaseInOut = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)

    // Waves: blush first, pink lags behind it
    const val WaveMs = 600
    const val WaveBlushDelayMs = 0
    const val WavePinkLagMs = 80

    // Pin, the hero: fades in fast and drops onto its tip with a slight overshoot
    const val PinFadeMs = 150
    const val PinDropDp = 24f
    const val PinStartScale = 0.85f
    const val PinDampingRatio = 0.7f  // 1f = no overshoot; 0.7f is about 5%
    const val PinStiffness = 380f     // settles in ~350 ms
    val PinSpring: SpringSpec<Float>
        get() = spring(dampingRatio = PinDampingRatio, stiffness = PinStiffness / (SlowMotion * SlowMotion))

    const val UtensilsDelayMs = 180
    const val UtensilsMs = 220

    // The two dashes flick outward from the pin's centre
    const val FlicksDelayMs = 320
    const val FlicksMs = 200
    const val FlickStartScale = 0.8f

    // Wordmark rises and fades in; the tagline follows
    const val WordmarkDelayMs = 380
    const val WordmarkMs = 420
    const val TextRiseDp = 16f
    const val TaglineLagMs = 100
    const val TaglineMs = 420

    // Doodles fade in one after another
    const val DoodlesDelayMs = 300
    const val DoodleStaggerMs = 70
    const val DoodleMs = 450

    // Sparkles twinkle once: brighten and grow to a peak, then settle smaller and dimmer
    const val SparklesDelayMs = 520
    const val SparkleStaggerMs = 80
    const val SparkleMs = 400
    const val SparklePeakAt = 0.4f     // fraction of SparkleMs
    const val SparkleStartScale = 0.6f
    const val SparklePeakScale = 1.15f

    // Exit into Home
    const val ExitMs = 250
    /** Switch status-bar icons to dark once the fade is mostly done, not at its start. */
    const val BarIconsSwitchDelayMs = 90
}
