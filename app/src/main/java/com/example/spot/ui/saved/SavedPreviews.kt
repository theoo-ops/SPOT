package com.example.spot.ui.saved

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
import com.example.spot.ui.theme.SpotTheme

// break-ui fixtures: they enter through SavedUiState, the same boundary the ViewModel uses.

private fun items(spots: List<FoodSpot>, withDistance: Boolean = true) = spots.mapIndexed { i, spot ->
    SavedItem(
        id = spot.id, name = spot.name, photo = spot.photo,
        distanceLabel = if (withDistance) Distance.format(Distance.meters(SpotGeo.ReferencePoint, spot.location)) else null,
        savedAt = 1_000_000L - i,
    )
}

private val Mockup = items(SeedFoodSpots.filter { it.id in setOf("mang-juans-carinderia", "cafe-dulce", "ricos-pizza") })

@Composable
private fun PreviewSaved(state: SavedUiState) {
    SpotTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            SavedScreen(state, {}, {}, {}, {}, {})
        }
    }
}

@Preview(name = "1 Mockup: three saved spots", widthDp = 360, heightDp = 760)
@Composable
private fun SavedMockupPreview() = PreviewSaved(SavedUiState(isLoading = false, items = Mockup))

@Preview(name = "2 No location (distances hidden)", widthDp = 360, heightDp = 760)
@Composable
private fun SavedNoLocationPreview() = PreviewSaved(SavedUiState(isLoading = false, items = items(SeedFoodSpots.take(3), withDistance = false)))

@Preview(name = "3 Loading", widthDp = 360, heightDp = 760)
@Composable
private fun SavedLoadingPreview() = PreviewSaved(SavedUiState(isLoading = true))

@Preview(name = "4 Empty", widthDp = 360, heightDp = 760)
@Composable
private fun SavedEmptyPreview() = PreviewSaved(SavedUiState(isLoading = false, items = emptyList()))

@Preview(name = "5 One saved spot", widthDp = 360, heightDp = 760)
@Composable
private fun SavedOnePreview() = PreviewSaved(SavedUiState(isLoading = false, items = Mockup.take(1)))

@Preview(name = "6 500 saved spots", widthDp = 360, heightDp = 760)
@Composable
private fun SavedManyPreview() = PreviewSaved(SavedUiState(isLoading = false, items = items(manySpots(500))))

@Preview(name = "7 Worst-case names (60 chars, emoji, accents)", widthDp = 360, heightDp = 900)
@Composable
private fun SavedWorstPreview() = PreviewSaved(SavedUiState(isLoading = false, items = items(WorstCaseSpots)))

@Preview(name = "8 Worst case, font 200%", widthDp = 360, heightDp = 1400, fontScale = 2f)
@Composable
private fun SavedWorstLargeFontPreview() = PreviewSaved(SavedUiState(isLoading = false, items = items(WorstCaseSpots)))

@Preview(name = "9 Empty, font 200%", widthDp = 360, heightDp = 900, fontScale = 2f)
@Composable
private fun SavedEmptyLargeFontPreview() = PreviewSaved(SavedUiState(isLoading = false, items = emptyList()))
