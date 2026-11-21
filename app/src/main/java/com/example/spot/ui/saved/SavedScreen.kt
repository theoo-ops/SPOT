package com.example.spot.ui.saved

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spot.R
import com.example.spot.ui.home.SpotCardSkeleton
import com.example.spot.ui.motion.LocalAnimationsEnabled
import com.example.spot.ui.motion.PressMotion
import kotlinx.coroutines.launch

@Composable
fun SavedRoute(
    onBack: () -> Unit,
    onOpenDetails: (String) -> Unit,
    onExplore: () -> Unit,
    viewModel: SavedViewModel = viewModel(factory = SavedViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(onBack = onBack) // system back = the back arrow
    SavedScreen(
        state = state,
        onBack = onBack,
        onOpenDetails = onOpenDetails,
        onExplore = onExplore,
        onRemove = viewModel::remove,
        onUndoRemove = viewModel::undoRemove,
    )
}

@Composable
fun SavedScreen(
    state: SavedUiState,
    onBack: () -> Unit,
    onOpenDetails: (String) -> Unit,
    onExplore: () -> Unit,
    onRemove: (SavedItem) -> Unit,
    onUndoRemove: (SavedItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val removedMessage = stringResource(R.string.saved_removed)
    val undoLabel = stringResource(R.string.saved_undo)

    // Remove right away, then offer Undo: the user stays in control without a confirmation dialog.
    val remove: (SavedItem) -> Unit = { item ->
        onRemove(item)
        scope.launch {
            snackbarHost.currentSnackbarData?.dismiss() // one Undo at a time: the latest removal
            // Material extends the duration automatically while TalkBack is on.
            val result = snackbarHost.showSnackbar(removedMessage, actionLabel = undoLabel)
            if (result == SnackbarResult.ActionPerformed) onUndoRemove(item)
        }
    }

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            SavedTopBar(onBack = onBack)
            SavedCountAnnouncer(isLoading = state.isLoading, count = state.items.size)

            val listState = rememberLazyListState() // saveable: survives rotation and tab switches
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp), // room for the snackbar
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when {
                    state.isLoading -> item(key = "loading", contentType = "loading") {
                        val loading = stringResource(R.string.saved_loading)
                        Column(
                            modifier = Modifier.clearAndSetSemantics { contentDescription = loading },
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) { repeat(3) { SpotCardSkeleton() } }
                    }

                    state.items.isEmpty() -> item(key = "empty", contentType = "empty") {
                        SavedEmptyState(onExplore = onExplore, modifier = Modifier.savedItemMotion(this))
                    }

                    else -> {
                        items(state.items, key = { it.id }, contentType = { "saved" }) { item ->
                            SavedSpotCard(
                                item = item,
                                onClick = { onOpenDetails(item.id) },
                                onRemove = { remove(item) },
                                modifier = Modifier.savedItemMotion(this),
                            )
                        }
                        item(key = "save-more", contentType = "pill") {
                            SaveMorePill(onClick = onExplore, modifier = Modifier.savedItemMotion(this))
                        }
                    }
                }
            }
        }
        SnackbarHost(snackbarHost, Modifier.align(Alignment.BottomCenter).padding(horizontal = 8.dp))
    }
}

private val EaseInOut = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)

/**
 * Removal: the card fades out (200 ms, ease-out) while the cards below slide up into its place.
 * Undo: the card fades back in. All of it off when the system's animations are off.
 */
@Composable
private fun Modifier.savedItemMotion(scope: LazyItemScope): Modifier {
    val animate = LocalAnimationsEnabled.current
    return with(scope) {
        if (animate) {
            animateItem(
                fadeInSpec = tween(SavedMotion.RemoveMs, easing = PressMotion.EaseOut),
                placementSpec = tween<IntOffset>(SavedMotion.SlideMs, easing = EaseInOut),
                fadeOutSpec = tween(SavedMotion.RemoveMs, easing = PressMotion.EaseOut),
            )
        } else {
            animateItem(fadeInSpec = null, placementSpec = null, fadeOutSpec = null)
        }
    }
}
