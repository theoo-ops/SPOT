package com.example.spot.ui.details

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.spot.R
import com.example.spot.ui.theme.SpotTheme

// break-ui fixtures: they enter through DetailsUiState, the same boundary the app uses.
// No spot has a photo yet, so ic_launcher_background stands in for "a photo exists".

private val Mockup = SpotDetails(
    id = "mang-juans-carinderia", name = "Mang Juan's Carinderia", category = "Filipino",
    rating = 4.6, reviewCount = 120, distanceMeters = 200.0,
    hours = "8:00 AM – 9:00 PM", address = "Golden Country Homes, BatStateU Alangilan",
    photo = R.drawable.ic_launcher_background, latitude = 13.78581, longitude = 121.07400,
)

/** 60-character name, unrated, no distance, no hours, very long address, no photo. */
private val Worst = Mockup.copy(
    id = "worst-long", name = "Kusina ni Aling Rosario at Kakanin sa Kanto — Golden Country",
    category = "Karinderya", rating = null, reviewCount = null, distanceMeters = null, hours = null,
    address = "Blk 12 Lot 34, Phase 3B, Golden Country Homes Subdivision, Brgy. Alangilan, Batangas City, Batangas 4200, Philippines",
    photo = null,
)

@Composable
private fun PreviewDetails(state: DetailsUiState) = SpotTheme {
    DetailsScreen(state, remember { SnackbarHostState() }, {}, {}, {})
}

@Preview(name = "1 Mockup (photo, 120 reviews, 200 m)", widthDp = 360, heightDp = 760)
@Composable
private fun DetailsMockupPreview() = PreviewDetails(DetailsUiState.Loaded(Mockup, isSaved = false))

@Preview(name = "2 Saved, missing photo (no View Photos)", widthDp = 360, heightDp = 760)
@Composable
private fun DetailsNoPhotoPreview() = PreviewDetails(DetailsUiState.Loaded(Mockup.copy(photo = null), isSaved = true))

@Preview(name = "3 Worst: 60-char name, long address, no hours/rating/distance", widthDp = 360, heightDp = 760)
@Composable
private fun DetailsWorstPreview() = PreviewDetails(DetailsUiState.Loaded(Worst, isSaved = false))

@Preview(name = "4 Font 200% (buttons pinned, content scrolls)", widthDp = 360, heightDp = 760, fontScale = 2f)
@Composable
private fun DetailsLargeFontPreview() = PreviewDetails(DetailsUiState.Loaded(Mockup.copy(name = Worst.name), isSaved = true))

@Preview(name = "5 Small screen 320x568", widthDp = 320, heightDp = 568)
@Composable
private fun DetailsSmallPreview() = PreviewDetails(DetailsUiState.Loaded(Mockup, isSaved = false))

@Preview(name = "6 Only the name (everything optional missing)", widthDp = 360, heightDp = 640)
@Composable
private fun DetailsBarePreview() = PreviewDetails(
    DetailsUiState.Loaded(Worst.copy(name = "Jo's", category = null, address = null), isSaved = false),
)

@Preview(name = "7 Loading", widthDp = 360, heightDp = 640)
@Composable
private fun DetailsLoadingPreview() = PreviewDetails(DetailsUiState.Loading)

@Preview(name = "8 Unknown id", widthDp = 360, heightDp = 640)
@Composable
private fun DetailsNotFoundPreview() = PreviewDetails(DetailsUiState.NotFound)
