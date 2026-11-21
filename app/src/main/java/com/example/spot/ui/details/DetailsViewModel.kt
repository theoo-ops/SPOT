package com.example.spot.ui.details

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.spot.data.FoodSpotRepository
import com.example.spot.data.model.FoodSpot
import com.example.spot.data.model.GeoPoint
import com.example.spot.data.saved.SavedSpotsRepository
import com.example.spot.di.AppGraph
import com.example.spot.location.Distance
import com.example.spot.location.UserLocationProvider
import com.example.spot.location.distanceOrigin
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DetailsUiState {
    data object Loading : DetailsUiState

    /** The id isn't in the data (removed spot, stale link). */
    data object NotFound : DetailsUiState

    @Immutable
    data class Loaded(val spot: SpotDetails, val isSaved: Boolean) : DetailsUiState
}

/** What the screen shows. A null field is missing data, and its part of the screen is hidden. */
@Immutable
data class SpotDetails(
    val id: String,
    val name: String,
    val category: String?,
    /** null = nobody has rated it yet: shows "New", like Home. */
    val rating: Double?,
    /** Not in FoodSpot yet, so always null for now and "(120 reviews)" stays hidden. */
    val reviewCount: Int? = null,
    /** null without a precise location fix. */
    val distanceMeters: Double?,
    val hours: String?,
    val address: String?,
    @param:DrawableRes val photo: Int?,
    val latitude: Double,
    val longitude: Double,
)

internal fun FoodSpot.toDetails(origin: GeoPoint?) = SpotDetails(
    id = id,
    name = name,
    category = category.takeIf { it.isNotBlank() },
    rating = rating,
    distanceMeters = origin?.let { Distance.meters(it, location) },
    hours = hours.takeIf { it.isNotBlank() },
    address = address.takeIf { it.isNotBlank() },
    photo = photo,
    latitude = latitude,
    longitude = longitude,
)

class DetailsViewModel(
    private val spotId: String,
    foodSpots: FoodSpotRepository,
    private val savedSpots: SavedSpotsRepository,
    locationProvider: UserLocationProvider,
) : ViewModel() {

    val uiState: StateFlow<DetailsUiState> = combine(
        flow { emit(foodSpots.getSpot(spotId)) },
        savedSpots.isSaved(spotId),
        locationProvider.state,
    ) { spot, saved, location ->
        if (spot == null) DetailsUiState.NotFound
        else DetailsUiState.Loaded(spot.toDetails(location.distanceOrigin), saved)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailsUiState.Loading)

    fun toggleSaved() {
        viewModelScope.launch { savedSpots.toggle(spotId) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                DetailsViewModel(
                    spotId = createSavedStateHandle().get<String>("id").orEmpty(), // the "details/{id}" argument
                    foodSpots = AppGraph.foodSpotRepository,
                    savedSpots = AppGraph.savedSpotsRepository,
                    locationProvider = AppGraph.userLocationProvider,
                )
            }
        }
    }
}
