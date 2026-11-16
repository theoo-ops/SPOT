package com.example.spot.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.spot.data.fake.SeedFoodSpots
import com.example.spot.location.Distance
import com.example.spot.location.FakeUserLocationProvider
import com.example.spot.ui.theme.SpotTheme

// Fixtures enter through HomeUiState, the same boundary the ViewModel uses.

private val DemoSpots: List<SpotItem> = SeedFoodSpots
    .map { it to Distance.meters(FakeUserLocationProvider.DefaultPoint, it.location) }
    .sortedBy { it.second }
    .map { (spot, m) -> spot.toItem(Distance.format(m)) }

/** Realistic worst cases, spread across the first rows (what's on screen). */
private val WorstSpots = listOf(
    SpotItem("w1", "Aling Rosario's Lutong Bahay at Kakanin sa Kanto ng Golden Country Homes Phase 2",
        "Karinderya", 4.95, Distance.format(8.0), null),
    SpotItem("w2", "Jo's", "Café", 5.0, Distance.format(995.0), null),
    SpotItem("w3", "Samgyupsal & Korean BBQ Unlimited — Alangilan", "Korean BBQ & Samgyupsal",
        3.0, Distance.format(12_449.0), null),
    SpotItem("w4", "Kape ☕ at Pandesal ni Mang Totoy", "Bakery", 4.25, Distance.format(1_050.0), null),
    SpotItem("w5", "Bánh Mì & Phở Corner", "Vietnamese", 0.0, Distance.format(250_000.0), null),
)

private val HundredSpots = List(100) { i ->
    SpotItem("h$i", "Food Spot #${i + 1}", listOf("Filipino", "Café", "Pizza", "Grill")[i % 4],
        4.0 + (i % 10) / 10.0, Distance.format(150.0 + i * 37), null)
}

@Composable
private fun PreviewHome(state: HomeUiState) {
    SpotTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            HomeScreen(state, {}, {}, {}, {}, {}, {})
        }
    }
}

@Preview(name = "1 Demo", widthDp = 360, heightDp = 800)
@Composable
private fun HomeDemoPreview() = PreviewHome(HomeUiState(isLoading = false, spots = DemoSpots))

@Preview(name = "2 Worst case", widthDp = 360, heightDp = 1100)
@Composable
private fun HomeWorstPreview() = PreviewHome(HomeUiState(isLoading = false, greeting = Greeting.Afternoon, spots = WorstSpots))

@Preview(name = "3 Worst case, font 200%", widthDp = 360, heightDp = 1600, fontScale = 2f)
@Composable
private fun HomeWorstLargeFontPreview() = PreviewHome(HomeUiState(isLoading = false, greeting = Greeting.Evening, spots = WorstSpots))

@Preview(name = "4 Empty chip (Bakery)", widthDp = 360, heightDp = 800)
@Composable
private fun HomeEmptyPreview() = PreviewHome(HomeUiState(isLoading = false, selectedCategory = "Bakery"))

@Preview(name = "5 One spot", widthDp = 360, heightDp = 800)
@Composable
private fun HomeOnePreview() = PreviewHome(HomeUiState(isLoading = false, spots = DemoSpots.take(1)))

@Preview(name = "6 100 spots", widthDp = 360, heightDp = 800)
@Composable
private fun HomeHundredPreview() = PreviewHome(HomeUiState(isLoading = false, spots = HundredSpots))

@Preview(name = "7 Loading", widthDp = 360, heightDp = 800)
@Composable
private fun HomeLoadingPreview() = PreviewHome(HomeUiState(isLoading = true))

@Preview(name = "8 Small phone 320dp", widthDp = 320, heightDp = 640)
@Composable
private fun HomeSmallPreview() = PreviewHome(HomeUiState(isLoading = false, spots = WorstSpots))
