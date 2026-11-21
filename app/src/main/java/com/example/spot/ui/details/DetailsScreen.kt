package com.example.spot.ui.details

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spot.R
import kotlinx.coroutines.launch

@Composable
fun DetailsRoute(
    onBack: () -> Unit,
    viewModel: DetailsViewModel = viewModel(factory = DetailsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val noMapsApp = stringResource(R.string.details_no_maps_app)
    DetailsScreen(
        state = state,
        snackbarHost = snackbarHost,
        onBack = onBack,
        onToggleSaved = viewModel::toggleSaved,
        onGetDirections = { spot ->
            if (!openDirections(context, spot.latitude, spot.longitude)) {
                scope.launch { snackbarHost.showSnackbar(noMapsApp) }
            }
        },
    )
}

/**
 * Full screen, edge to edge: the photo runs behind the status bar, so this screen handles its own
 * insets (SpotApp gives it no padding). Hero and details scroll; the two actions stay pinned.
 */
@Composable
fun DetailsScreen(
    state: DetailsUiState,
    snackbarHost: SnackbarHostState,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
    onGetDirections: (SpotDetails) -> Unit,
    modifier: Modifier = Modifier,
) {
    val horizontalSafe = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarHeight = statusTop + TopBarHeight
    val loaded = state as? DetailsUiState.Loaded
    var showPhoto by rememberSaveable { mutableStateOf(false) }

    BoxWithConstraints(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
        val heroHeight = maxHeight * HeroFraction // landscape and split screen: shorter photo, more room for details
        val scroll = rememberScrollState()
        val density = LocalDensity.current
        // Once the photo has scrolled up under the buttons, give the status bar a plain background.
        val collapsed by remember(heroHeight, topBarHeight) {
            derivedStateOf { scroll.value >= with(density) { (heroHeight - topBarHeight).toPx() } }
        }
        val hasPhoto = loaded?.spot?.photo != null
        StatusBarIcons(light = hasPhoto && !collapsed && state !is DetailsUiState.NotFound)

        when (state) {
            DetailsUiState.NotFound -> NotFoundContent(onBack, Modifier.windowInsetsPadding(horizontalSafe))
            else -> Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
                        if (loaded != null) {
                            DetailsHero(loaded.spot.photo, heroHeight, statusTop)
                            DetailsBody(loaded.spot, Modifier.windowInsetsPadding(horizontalSafe))
                        } else {
                            LoadingContent(heroHeight, Modifier.windowInsetsPadding(horizontalSafe))
                        }
                    }
                    SnackbarHost(snackbarHost, Modifier.align(Alignment.BottomCenter).windowInsetsPadding(horizontalSafe).padding(8.dp))
                }
                if (loaded != null) {
                    DetailsActions(
                        hasPhoto = hasPhoto,
                        showTopDivider = scroll.canScrollForward, // more content hides behind the actions
                        onGetDirections = { onGetDirections(loaded.spot) },
                        onViewPhotos = { showPhoto = true },
                        modifier = Modifier.windowInsetsPadding(horizontalSafe),
                    )
                }
            }
        }

        // Pinned back and heart, over the photo (or over a plain bar once it scrolled away).
        if (hasPhoto && !collapsed) TopScrim(topBarHeight)
        if (collapsed && state !is DetailsUiState.NotFound) {
            Box(
                Modifier.fillMaxWidth().height(topBarHeight).background(MaterialTheme.colorScheme.surfaceContainerLowest),
            ) {
                HorizontalDivider(Modifier.align(Alignment.BottomCenter), color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(horizontalSafe)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BackButton(onBack)
            if (loaded != null) {
                HeartButton(saved = loaded.isSaved, name = loaded.spot.name, onToggle = onToggleSaved)
            } else {
                Spacer(Modifier)
            }
        }
    }

    val photo = loaded?.spot?.photo
    if (showPhoto && loaded != null && photo != null) {
        PhotoViewer(photo = photo, name = loaded.spot.name, onDismiss = { showPhoto = false })
    }
}

/** White status bar icons over the photo's scrim; dark ones everywhere else (and again on leaving). */
@Composable
private fun StatusBarIcons(light: Boolean) {
    val activity = LocalActivity.current ?: return // previews
    val view = LocalView.current
    DisposableEffect(light) {
        val controller = WindowCompat.getInsetsController(activity.window, view)
        controller.isAppearanceLightStatusBars = !light
        onDispose { controller.isAppearanceLightStatusBars = true }
    }
}

private const val HeroFraction = 0.4f
private val TopBarHeight = 64.dp // 48dp buttons + 8dp above and below
