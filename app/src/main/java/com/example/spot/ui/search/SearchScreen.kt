package com.example.spot.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spot.R
import com.example.spot.ui.components.CategoryChips
import com.example.spot.ui.home.SpotCard
import com.example.spot.ui.home.SpotCardSkeleton

/**
 * The Explore tab. [focusRequested] is true when Home's search field sent the user here:
 * focus the field and open the keyboard once. Opening the tab directly doesn't.
 */
@Composable
fun SearchRoute(
    focusRequested: Boolean,
    onFocusRequestHandled: () -> Unit,
    onBack: () -> Unit,
    onOpenDetails: (String) -> Unit,
    viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(onBack = onBack) // system back = the back arrow (keeps the query for next time)
    SearchScreen(
        query = viewModel.query,
        state = state,
        focusRequested = focusRequested,
        onFocusRequestHandled = onFocusRequestHandled,
        onQueryChange = viewModel::onQueryChange,
        onClearQuery = viewModel::clearQuery,
        onCategorySelect = viewModel::selectCategory,
        onBack = onBack,
        onOpenDetails = onOpenDetails,
    )
}

@Composable
fun SearchScreen(
    query: String,
    state: SearchUiState,
    focusRequested: Boolean,
    onFocusRequestHandled: () -> Unit,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onCategorySelect: (String?) -> Unit,
    onBack: () -> Unit,
    onOpenDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusRequested) {
        if (focusRequested) {
            focusRequester.requestFocus()
            keyboard?.show()
            onFocusRequestHandled()
        }
    }

    // Dragging the list hides the keyboard (the field keeps focus; tapping it brings the keyboard back).
    val listState = rememberLazyListState()
    val dragging by listState.interactionSource.collectIsDraggedAsState()
    LaunchedEffect(dragging) { if (dragging) keyboard?.hide() }

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
            SearchField(
                query = query,
                onQueryChange = onQueryChange,
                onClear = {
                    onClearQuery()
                    focusRequester.requestFocus() // keep typing right away
                },
                onSearch = { keyboard?.hide() },
                focusRequester = focusRequester,
                modifier = Modifier.weight(1f),
            )
        }

        CategoryChips(
            categories = state.categories,
            selected = state.selectedCategory,
            onSelect = onCategorySelect,
            contentPadding = PaddingValues(horizontal = 16.dp),
        )

        ResultCountAnnouncer(isLoading = state.isLoading, count = state.results.size)

        LazyColumn(
            state = listState,
            // Only the content moves above the keyboard; the bottom bar stays put behind it.
            modifier = Modifier.weight(1f).fillMaxWidth().imePadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when {
                state.isLoading -> item(key = "loading", contentType = "loading") {
                    val loading = stringResource(R.string.home_loading)
                    Column(
                        modifier = Modifier.clearAndSetSemantics { contentDescription = loading },
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) { repeat(3) { SpotCardSkeleton() } }
                }

                state.results.isEmpty() -> item(key = "empty", contentType = "empty") {
                    NoResults(
                        query = state.query,
                        category = state.selectedCategory,
                        onClearQuery = {
                            onClearQuery()
                            focusRequester.requestFocus()
                        },
                        onShowAll = { onCategorySelect(null) },
                    )
                }

                else -> items(state.results, key = { "spot-${it.id}" }, contentType = { "spot" }) { spot ->
                    SpotCard(spot, onClick = { onOpenDetails(spot.id) })
                }
            }
        }
    }
}
