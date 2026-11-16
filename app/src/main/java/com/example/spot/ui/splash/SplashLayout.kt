package com.example.spot.ui.splash

import androidx.annotation.DrawableRes
import com.example.spot.R

internal data class DoodleSpec(
    @param:DrawableRes val res: Int,
    val x: Float, val y: Float,   // centre, as a fraction of screen width / height
    val width: Float,             // fraction of `unit`
    val rotation: Float,
)

internal data class SparkleSpec(val x: Float, val y: Float, val width: Float)

/**
 * Positions are fractions of the screen; sizes are fractions of `unit`,
 * where unit = min(width, height * 9/16) so wide screens don't blow the logo up.
 * To match Figma exactly: x = layer centre x / frame width, y = layer centre y / frame height.
 * Sizes: the exports share a 378dp-wide Figma frame (same width as the waves), so a layer's
 * width fraction = its exported width in dp / 378.
 */
internal object SplashLayout {
    const val WideScreenRatio = 0.5625f

    // Logo lockup: pin, wordmark and tagline stacked as one group, so they keep their
    // proportions on any screen shape. Its centre sits at LockupY of the free area
    // between the status bar and the top of the wave.
    const val LockupY = 0.53f
    const val PinToWordmarkGap = 0.165f     // of unit
    const val WordmarkToTaglineGap = 0.065f // of unit

    const val PinWidth = 0.243f             // 92dp / 378
    const val PinTipY = 1f                  // spring scales from the pin's tip (bottom of ic_spot_pin)
    const val UtensilsWidthRatio = 0.74f    // fraction of PinWidth to fit inside pin head
    const val UtensilsOffsetYRatio = 0.174f // vertical offset ratio to center utensils in pin head
    const val FlicksWidthRatio = 0.217f     // fraction of PinWidth (20dp / 92dp, the size you've been seeing)
    const val FlicksOffsetYRatio = -1.2f    // vertical offset relative to flicks height so dashes float above pin head

    const val WordmarkWidth = 0.455f        // 172dp / 378

    const val TaglineSize = 0.042f   // of unit; part of the logo lockup
    const val TaglineMaxWidth = 0.62f
    const val TaglineMinDp = 13f     // floor so landscape stays readable

    const val WaveHeight = 0.22f     // blush layer, of screen height; pink keeps its exported ratio to it

    // The doodle and sparkle exports already carry their faintness (strokeAlpha 0.16),
    // so these multiply on top of it: 1f = exactly as exported.
    const val DoodleAlpha = 1f
    const val SparklePeakAlpha = 1f
    const val SparkleRestAlpha = 0.7f

    val Doodles = listOf(
        DoodleSpec(R.drawable.ic_doodle_pizza, x = 0.15f, y = 0.11f, width = 0.148f, rotation = -20f), // 56dp
        DoodleSpec(R.drawable.ic_doodle_cup, x = 0.80f, y = 0.07f, width = 0.169f, rotation = 12f),    // 64dp
        DoodleSpec(R.drawable.ic_doodle_ramen, x = 0.08f, y = 0.37f, width = 0.127f, rotation = -8f),  // 48dp
        DoodleSpec(R.drawable.ic_doodle_pizza, x = 0.18f, y = 0.75f, width = 0.148f, rotation = 15f),  // 56dp
    )

    val Sparkles = listOf(  // ic_sparkle is 32dp = 0.085
        SparkleSpec(x = 0.88f, y = 0.47f, width = 0.085f),
        SparkleSpec(x = 0.62f, y = 0.74f, width = 0.085f),
        SparkleSpec(x = 0.12f, y = 0.55f, width = 0.085f),
    )
}
