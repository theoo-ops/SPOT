package com.example.spot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Light-only, no dynamic color: the mockup's burgundy is the brand,
 * so Android 12+ wallpaper colors must not replace it.
 */
@Composable
fun SpotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SpotLightColors,
        typography = SpotTypography,
        content = content,
    )
}
