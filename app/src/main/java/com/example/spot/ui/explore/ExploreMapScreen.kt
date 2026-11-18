package com.example.spot.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spot.R
import com.example.spot.data.model.GeoPoint
import com.example.spot.location.LocationPermissions
import com.example.spot.location.LocationState
import com.example.spot.location.PermissionOutcome
import com.example.spot.location.SpotGeo
import com.example.spot.location.rememberLocationPermissionRequest
import com.example.spot.ui.components.CategoryChips
import com.example.spot.ui.motion.LocalAnimationsEnabled
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ExploreMapRoute(
    onBack: () -> Unit,
    onOpenDetails: (String) -> Unit,
    viewModel: ExploreViewModel = viewModel(factory = ExploreViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // "Don't ask again" is only knowable right after a denial, so remember it for this screen.
    var deniedForever by rememberSaveable { mutableStateOf(false) }
    val requestPermission = rememberLocationPermissionRequest { outcome ->
        deniedForever = outcome == PermissionOutcome.DeniedPermanently
        viewModel.refreshLocation()
    }

    // Ask when the user opens the map (never at app start), once per visit, not again on rotation.
    var askedThisVisit by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        logIfMapsKeyMissing(context)
        if (!askedThisVisit) {
            askedThisVisit = true
            if (!LocationPermissions.isGranted(context)) requestPermission()
        }
    }

    ExploreMapScreen(
        state = state,
        deniedForever = deniedForever,
        onBack = onBack,
        onCategorySelect = viewModel::selectCategory,
        onSpotSelect = viewModel::selectSpot,
        onClearSelection = viewModel::clearSelection,
        onOpenDetails = onOpenDetails,
        onRequestPermission = requestPermission,
        onOpenAppSettings = { context.startActivity(LocationPermissions.appSettingsIntent(context)) },
        onOpenLocationSettings = { context.startActivity(LocationPermissions.locationSettingsIntent()) },
    )
}

@Composable
fun ExploreMapScreen(
    state: ExploreUiState,
    deniedForever: Boolean,
    onBack: () -> Unit,
    onCategorySelect: (String?) -> Unit,
    onSpotSelect: (String) -> Unit,
    onClearSelection: () -> Unit,
    onOpenDetails: (String) -> Unit,
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animate = LocalAnimationsEnabled.current
    val scope = rememberCoroutineScope()
    val location = state.location
    val userPoint = (location as? LocationState.Available)?.point

    // Saveable: the camera survives rotation. First position: the user if known, else the reference point.
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom((userPoint ?: SpotGeo.ReferencePoint).toLatLng(), ExploreMotion.CameraZoom)
    }
    // Center on the user once, when the first fix arrives, unless they already moved the map.
    var centeredOnUser by rememberSaveable { mutableStateOf(userPoint != null) }
    LaunchedEffect(userPoint) {
        if (userPoint != null && !centeredOnUser) {
            centeredOnUser = true
            if (camera.cameraMoveStartedReason != CameraMoveStartedReason.GESTURE) {
                camera.moveOrAnimate(CameraUpdateFactory.newLatLngZoom(userPoint.toLatLng(), ExploreMotion.CameraZoom), animate)
            }
        }
    }

    var mapLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(MapLoadTimeoutMs)
        if (!mapLoaded) logMapDidNotLoad()
    }

    val online by rememberIsOnline()
    var dismissed by rememberSaveable { mutableStateOf(emptySet<String>()) }
    val banner = when {
        !online -> BannerKind.Offline
        location is LocationState.PermissionDenied ->
            if (deniedForever) BannerKind.PermissionDeniedForever else BannerKind.PermissionDenied
        location is LocationState.Unavailable -> BannerKind.LocationServicesOff
        location is LocationState.Available && !location.isPrecise -> BannerKind.Approximate
        else -> null
    }?.takeUnless { it.name in dismissed }

    val onLocate: () -> Unit = {
        when (location) {
            is LocationState.Available -> scope.launch {
                camera.moveOrAnimate(CameraUpdateFactory.newLatLngZoom(location.point.toLatLng(), ExploreMotion.CameraZoom), animate)
            }
            LocationState.PermissionDenied -> if (deniedForever) onOpenAppSettings() else onRequestPermission()
            LocationState.Unavailable -> onOpenLocationSettings()
            LocationState.Loading -> Unit
        }
    }

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ExploreTopBar(onBack = onBack, onFilter = { /* TODO: filters sheet (later step) */ })

        Box(Modifier.weight(1f).fillMaxWidth()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = camera,
                contentDescription = stringResource(R.string.explore_map_description),
                properties = remember { MapProperties(mapStyleOptions = MapStyleOptions(MutedMapStyle)) },
                uiSettings = remember {
                    MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false,
                        mapToolbarEnabled = false,
                        indoorLevelPickerEnabled = false,
                    )
                },
                // Keeps Google's logo and camera centring clear of the chips and the locate button.
                contentPadding = PaddingValues(top = 64.dp, bottom = 8.dp),
                onMapClick = { onClearSelection() },
                onMapLoaded = { mapLoaded = true },
            ) {
                state.spots.forEach { spot ->
                    key(spot.id) {
                        val selected = spot.id == state.selected?.id
                        MarkerComposable(
                            spot.id, selected,
                            state = rememberUpdatedMarkerState(spot.position.toLatLng()),
                            title = spot.name,
                            contentDescription = spot.name,
                            zIndex = if (selected) 2f else 1f,
                            onClick = { onSpotSelect(spot.id); true }, // true: no default info window
                        ) { SpotPin(selected) }
                    }
                }
                if (location is LocationState.Available) {
                    val here = location.point.toLatLng()
                    Circle(
                        center = here,
                        radius = location.accuracyMeters.toDouble(),
                        fillColor = UserHaloFill,
                        strokeColor = UserHaloStroke,
                        strokeWidth = 2f,
                    )
                    MarkerComposable(
                        "me",
                        state = rememberUpdatedMarkerState(here),
                        title = stringResource(R.string.cd_your_location),
                        contentDescription = stringResource(R.string.cd_your_location),
                        anchor = Offset(0.5f, 0.5f),
                        zIndex = 3f,
                        onClick = { true },
                    ) { UserDot() }
                }
            }

            state.selected?.let { spot ->
                CalloutAnchoredToMarker(spot, camera, onClick = { onOpenDetails(spot.id) })
            }

            Column(Modifier.fillMaxWidth().align(Alignment.TopCenter)) {
                CategoryChips(
                    categories = state.categories,
                    selected = state.selectedCategory,
                    onSelect = onCategorySelect,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                )
                banner?.let { kind ->
                    LocationBanner(
                        kind = kind,
                        onAction = {
                            when (kind) {
                                BannerKind.PermissionDenied, BannerKind.Approximate -> onRequestPermission()
                                BannerKind.PermissionDeniedForever -> onOpenAppSettings()
                                BannerKind.LocationServicesOff -> onOpenLocationSettings()
                                BannerKind.Offline -> Unit
                            }
                        },
                        onDismiss = { dismissed = dismissed + kind.name },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            if (!state.isLoadingSpots && state.spots.isEmpty() && state.selectedCategory != null) {
                EmptyMapMessage(
                    category = state.selectedCategory,
                    onShowAll = { onCategorySelect(null) },
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
            }

            LocateButton(
                isLocating = location is LocationState.Loading,
                onClick = onLocate,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        }
    }
}

/** Keeps the callout's bottom tip just above the selected pin as the camera moves. */
@Composable
private fun CalloutAnchoredToMarker(spot: MapSpot, camera: CameraPositionState, onClick: () -> Unit) {
    val gapPx = with(LocalDensity.current) { (SelectedPinSize + 2.dp).roundToPx() }
    val target = remember(spot.position) { spot.position.toLatLng() }
    SpotCallout(
        spot = spot,
        onClick = onClick,
        modifier = Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            layout(constraints.maxWidth, constraints.maxHeight) {
                camera.position // read: re-place on every camera move (layout only, no recomposition)
                val point = camera.projection?.toScreenLocation(target) ?: return@layout
                placeable.place(point.x - placeable.width / 2, point.y - gapPx - placeable.height)
            }
        },
    )
}

private suspend fun CameraPositionState.moveOrAnimate(update: CameraUpdate, animate: Boolean) {
    if (animate) animate(update) else move(update) // jump when the system's animations are off
}

internal fun GeoPoint.toLatLng() = LatLng(latitude, longitude)

/** Light, muted style: fewer POIs and transit labels so the burgundy pins stand out. */
private const val MutedMapStyle = """[
  {"featureType":"poi","elementType":"labels","stylers":[{"visibility":"off"}]},
  {"featureType":"poi.business","stylers":[{"visibility":"off"}]},
  {"featureType":"transit","stylers":[{"visibility":"off"}]},
  {"elementType":"geometry","stylers":[{"saturation":-30},{"lightness":10}]}
]"""
