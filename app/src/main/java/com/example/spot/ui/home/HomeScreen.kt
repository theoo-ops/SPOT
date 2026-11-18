package com.example.spot.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spot.R
import com.example.spot.ui.components.CategoryChips

private val Gutter = 20.dp

@Composable
fun HomeRoute(
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onViewMapClick: () -> Unit,
    onSeeAllClick: () -> Unit,
    onSpotClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onCategorySelect = viewModel::selectCategory,
        onSearchClick = onSearchClick,
        onProfileClick = onProfileClick,
        onViewMapClick = onViewMapClick,
        onSeeAllClick = onSeeAllClick,
        onSpotClick = onSpotClick,
    )
}

/** Stateless; one LazyColumn for the whole screen (the chip row scrolls sideways inside it). */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onCategorySelect: (String?) -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onViewMapClick: () -> Unit,
    onSeeAllClick: () -> Unit,
    onSpotClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemPadding = Modifier.padding(horizontal = Gutter)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header", contentType = "header") {
            HomeHeader(state.greeting, onProfileClick, itemPadding)
        }
        item(key = "search", contentType = "search") {
            SearchField(onSearchClick, itemPadding.padding(top = 8.dp))
        }
        item(key = "chips", contentType = "chips") {
            // Full-bleed: chips scroll to the screen edges, padding lives inside the row.
            CategoryChips(
                categories = state.categories,
                selected = state.selectedCategory,
                onSelect = onCategorySelect,
                contentPadding = PaddingValues(horizontal = Gutter),
            )
        }
        item(key = "map", contentType = "map") {
            MapPreviewCard(onViewMapClick, itemPadding)
        }
        item(key = "nearby-header", contentType = "section") {
            SectionHeader(onSeeAllClick, itemPadding.padding(top = 8.dp))
        }
        when {
            state.isLoading -> item(key = "loading", contentType = "loading") {
                val loading = stringResource(R.string.home_loading)
                Column(
                    modifier = itemPadding.clearAndSetSemantics { contentDescription = loading },
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    repeat(3) { SpotCardSkeleton() }
                }
            }

            state.spots.isEmpty() -> item(key = "empty", contentType = "empty") {
                EmptyCategory(
                    category = state.selectedCategory,
                    onShowAll = { onCategorySelect(null) },
                    modifier = itemPadding,
                )
            }

            else -> items(state.spots, key = { "spot-${it.id}" }, contentType = { "spot" }) { spot ->
                SpotCard(spot, onClick = { onSpotClick(spot.id) }, modifier = itemPadding)
            }
        }
    }
}
