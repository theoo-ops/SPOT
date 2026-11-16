package com.example.spot.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.spot.R

// Bundled static Inter files in res/font (see res/font/README.md).
val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_italic, FontWeight.Normal, FontStyle.Italic),
)

private fun inter(size: Int, line: Int, weight: FontWeight, tracking: Double = 0.0) = TextStyle(
    fontFamily = Inter,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
)

// Mapped to what the mockup shows:
//   headlineSmall  "Good morning!", "Profile"
//   titleLarge     top-bar titles ("Explore Map", "Saved Places"), detail name
//   titleMedium    section headers ("Nearby Food Spots"), list item names
//   bodyMedium     meta lines ("Filipino • 200 m away")
//   labelLarge     buttons ("Get Directions"), chips
//   labelMedium    bottom-bar labels
val SpotTypography = Typography(
    displayLarge = inter(57, 64, FontWeight.Bold, -0.25),
    displayMedium = inter(45, 52, FontWeight.Bold),
    displaySmall = inter(36, 44, FontWeight.Bold),

    headlineLarge = inter(32, 40, FontWeight.Bold),
    headlineMedium = inter(28, 36, FontWeight.Bold),
    headlineSmall = inter(24, 32, FontWeight.SemiBold),

    titleLarge = inter(20, 28, FontWeight.SemiBold),
    titleMedium = inter(16, 24, FontWeight.SemiBold),
    titleSmall = inter(14, 20, FontWeight.Medium),

    bodyLarge = inter(16, 24, FontWeight.Normal),
    bodyMedium = inter(14, 20, FontWeight.Normal),
    bodySmall = inter(12, 16, FontWeight.Normal),

    labelLarge = inter(14, 20, FontWeight.Medium),
    labelMedium = inter(12, 16, FontWeight.Medium),
    labelSmall = inter(11, 16, FontWeight.Medium),
)
