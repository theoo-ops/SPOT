package com.example.spot.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spot.data.FoodCategories
import com.example.spot.data.model.GeoPoint
import com.example.spot.location.Distance
import com.example.spot.ui.components.CategoryChips
import com.example.spot.ui.theme.SpotTheme

// break-ui fixtures for the parts GoogleMap can't render in previews: callout, chips, banners, buttons.

private val Here = GeoPoint(13.7840, 121.0740)
private val Demo = MapSpot("mang-juans", "Mang Juan's Carinderia", "Filipino", 4.6, Here, Distance.format(200.0))
/** 60 characters, no ratings yet, no location (distance hidden). */
private val Worst = MapSpot(
    "worst", "Kusina ni Aling Rosario at Kakanin sa Kanto — Golden Country", "Karinderya",
    rating = null, position = Here, distanceLabel = null,
)

@Composable
private fun OnMap(content: @Composable () -> Unit) {
    SpotTheme {
        Box(Modifier.background(Color(0xFFEAF1EA)).padding(16.dp)) { content() }
    }
}

@Preview(name = "Callout: demo")
@Composable
private fun CalloutDemoPreview() = OnMap { SpotCallout(Demo, onClick = {}) }

@Preview(name = "Callout: 60-char name, no rating, no distance")
@Composable
private fun CalloutWorstPreview() = OnMap { SpotCallout(Worst, onClick = {}) }

@Preview(name = "Callout: worst, font 200%", fontScale = 2f, widthDp = 360)
@Composable
private fun CalloutWorstLargePreview() = OnMap { SpotCallout(Worst, onClick = {}) }

@Preview(name = "Chips: font 200%", fontScale = 2f, widthDp = 360)
@Composable
private fun ChipsLargePreview() = OnMap {
    CategoryChips(FoodCategories.all, selected = "Karinderya", onSelect = {}, contentPadding = PaddingValues(0.dp))
}

@Preview(name = "Banners", widthDp = 360)
@Composable
private fun BannersPreview() = SpotTheme {
    Column(
        Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BannerKind.entries.forEach { LocationBanner(it, onAction = {}, onDismiss = {}) }
    }
}

@Preview(name = "Banners: font 200%", widthDp = 360, fontScale = 2f)
@Composable
private fun BannersLargePreview() = SpotTheme {
    Column(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        LocationBanner(BannerKind.PermissionDeniedForever, onAction = {}, onDismiss = {})
    }
}

@Preview(name = "Empty category + locate buttons")
@Composable
private fun EmptyAndLocatePreview() = OnMap {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        EmptyMapMessage("Bakery", onShowAll = {})
        LocateButton(isLocating = false, onClick = {})
        LocateButton(isLocating = true, onClick = {})
    }
}
