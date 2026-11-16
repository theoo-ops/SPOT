package com.example.spot.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ---- Raw palette, sampled from the SPOT mockup ----
// Burgundy family
val Burgundy = Color(0xFF7A1632)        // splash bg, selected tab, primary buttons, headings
val BurgundyDeep = Color(0xFF4A0C1D)    // text/icons on blush containers
val Rose = Color(0xFF8C4A58)            // muted secondary accent
val RoseLight = Color(0xFFFFB1C1)       // burgundy as seen on dark (inverse)

// Blush / surfaces
val Blush = Color(0xFFF9DDE3)           // "Filipino" tag, "Save more food spots" banner
val BlushSoft = Color(0xFFFCE8EC)
val OffWhite = Color(0xFFFFF8F8)        // screen background (warm, faintly pink)
val White = Color(0xFFFFFFFF)           // cards, bottom bar, search field

// Neutrals (warm-tinted so they sit with the burgundy)
val Ink = Color(0xFF231A1C)             // primary text
val InkMuted = Color(0xFF6B5E61)        // secondary text, unselected tabs (6.2:1 on white)
val OutlineStrong = Color(0xFFA88A92)   // real component boundaries (3.1:1 on white)
val OutlineSoft = Color(0xFFEBD3D9)     // chip borders, dividers (decorative)

// Not part of the M3 scheme: rating star
val StarYellow = Color(0xFFF5B800)

val SpotLightColors = lightColorScheme(
    primary = Burgundy,
    onPrimary = White,
    primaryContainer = Blush,
    onPrimaryContainer = BurgundyDeep,
    inversePrimary = RoseLight,

    secondary = Rose,
    onSecondary = White,
    secondaryContainer = BlushSoft,
    onSecondaryContainer = Color(0xFF3B0F19),

    tertiary = Color(0xFF7A5900),
    onTertiary = White,
    tertiaryContainer = Color(0xFFFFE08A),
    onTertiaryContainer = Color(0xFF261A00),

    background = OffWhite,
    onBackground = Ink,
    surface = OffWhite,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF6DEE3),
    onSurfaceVariant = InkMuted,
    surfaceTint = Burgundy,

    surfaceContainerLowest = White,
    surfaceContainerLow = Color(0xFFFFF1F3),
    surfaceContainer = Color(0xFFFCEBEE),
    surfaceContainerHigh = Color(0xFFF9E4E8),
    surfaceContainerHighest = Color(0xFFF6DEE3),
    surfaceBright = OffWhite,
    surfaceDim = Color(0xFFEFD8DC),

    inverseSurface = Color(0xFF382E30),
    inverseOnSurface = Color(0xFFFCEEF0),

    outline = OutlineStrong,
    outlineVariant = OutlineSoft,
    scrim = Color(0xFF000000),

    error = Color(0xFFBA1A1A),
    onError = White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)
