package com.example.spot.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.spot.data.fake.SeedFoodSpots
import com.example.spot.data.fake.WorstCaseSpots
import com.example.spot.data.fake.manySpots
import com.example.spot.data.model.FoodSpot
import com.example.spot.location.Distance
import com.example.spot.location.SpotGeo
import com.example.spot.ui.home.SpotItem
import com.example.spot.ui.home.toItem
import com.example.spot.ui.theme.SpotTheme

// break-ui fixtures: they enter through SearchUiState, the same boundary the ViewModel uses.

private fun items(spots: List<FoodSpot>, withDistance: Boolean = true): List<SpotItem> =
    spots.map { it to Distance.meters(SpotGeo.ReferencePoint, it.location) }
        .sortedBy { it.second }
        .map { (spot, m) -> spot.toItem(if (withDistance) Distance.format(m) else null) }

private val Pizza = items(SeedFoodSpots.filter { it.category == "Pizza" })
private val Worst = items(WorstCaseSpots)

@Composable
private fun PreviewSearch(query: String, state: SearchUiState) {
    SpotTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            SearchScreen(query, state.copy(query = query), false, {}, {}, {}, {}, {}, {})
        }
    }
}

@Preview(name = "1 Mockup: \"Pizza\"", widthDp = 360, heightDp = 760)
@Composable
private fun SearchPizzaPreview() = PreviewSearch("Pizza", SearchUiState(isLoading = false, results = Pizza))

@Preview(name = "2 Empty query, no location (rating order, no distances)", widthDp = 360, heightDp = 760)
@Composable
private fun SearchAllNoLocationPreview() = PreviewSearch(
    "", SearchUiState(isLoading = false, results = items(SeedFoodSpots, withDistance = false).sortedByDescending { it.rating ?: -1.0 }),
)

@Preview(name = "3 Loading", widthDp = 360, heightDp = 760)
@Composable
private fun SearchLoadingPreview() = PreviewSearch("caf", SearchUiState(isLoading = true))

@Preview(name = "4 No results: long query with accents, emoji, symbols", widthDp = 360, heightDp = 760)
@Composable
private fun SearchNoResultsPreview() = PreviewSearch(
    "Ñaña’s crème brûlée 🍮 & halo-halo (100%) near BatStateU Alangilan gate #2!!!",
    SearchUiState(isLoading = false, results = emptyList()),
)

@Preview(name = "5 Empty category (Bakery)", widthDp = 360, heightDp = 760)
@Composable
private fun SearchEmptyCategoryPreview() = PreviewSearch("", SearchUiState(isLoading = false, selectedCategory = "Bakery"))

@Preview(name = "6 One result", widthDp = 360, heightDp = 760)
@Composable
private fun SearchOnePreview() = PreviewSearch("mang juan", SearchUiState(isLoading = false, results = items(SeedFoodSpots.take(1))))

@Preview(name = "7 1,000 results", widthDp = 360, heightDp = 760)
@Composable
private fun SearchThousandPreview() = PreviewSearch("", SearchUiState(isLoading = false, results = items(manySpots(1_000))))

@Preview(name = "8 Worst-case names (60 chars, no rating, emoji)", widthDp = 360, heightDp = 900)
@Composable
private fun SearchWorstPreview() = PreviewSearch("", SearchUiState(isLoading = false, results = Worst))

@Preview(name = "9 Worst case, font 200%", widthDp = 360, heightDp = 1400, fontScale = 2f)
@Composable
private fun SearchWorstLargeFontPreview() = PreviewSearch("Kusina ni Aling Rosario", SearchUiState(isLoading = false, results = Worst))

@Preview(name = "10 Very long query, small phone", widthDp = 320, heightDp = 640)
@Composable
private fun SearchLongQueryPreview() = PreviewSearch(
    "pizza pizza pizza pizza pizza pizza pizza pizza pizza", SearchUiState(isLoading = false, results = Pizza),
)
