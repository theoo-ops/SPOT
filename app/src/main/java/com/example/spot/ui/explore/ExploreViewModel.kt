package com.example.spot.ui.explore

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.spot.data.FoodCategories
import com.example.spot.data.FoodSpotRepository
import com.example.spot.data.model.GeoPoint
import com.example.spot.di.AppGraph
import com.example.spot.location.Distance
import com.example.spot.location.LocationState
import com.example.spot.location.UserLocationProvider
import com.example.spot.location.distanceOrigin
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@Immutable
data class MapSpot(
    val id: String,
    val name: String,
    val category: String,
    val rating: Double?,
    val position: GeoPoint,
    /** Null when there is no trustworthy location. */
    val distanceLabel: String?,
)

@Immutable
data class ExploreUiState(
    val isLoadingSpots: Boolean = true,
    val location: LocationState = LocationState.Loading,
    val categories: List<String> = FoodCategories.all,
    /** null = "All". */
    val selectedCategory: String? = null,
    /** Spots in the selected category: one marker each. */
    val spots: List<MapSpot> = emptyList(),
    val selected: MapSpot? = null,
)

/** Category and selected marker live in SavedStateHandle, so they survive rotation and process death. */
class ExploreViewModel(
    repository: FoodSpotRepository,
    private val locationProvider: UserLocationProvider,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    private val categoryFlow = savedState.getStateFlow<String?>(KeyCategory, null)
    private val selectedIdFlow = savedState.getStateFlow<String?>(KeySelected, null)

    val uiState: StateFlow<ExploreUiState> = combine(
        repository.observeSpots(),
        locationProvider.state,
        categoryFlow,
        selectedIdFlow,
    ) { spots, location, category, selectedId ->
        val origin = location.distanceOrigin
        val visible = spots
            .filter { category == null || it.category == category }
            .map {
                MapSpot(it.id, it.name, it.category, it.rating, it.location, Distance.labelOrNull(origin, it.location))
            }
        ExploreUiState(
            isLoadingSpots = false,
            location = location,
            selectedCategory = category,
            spots = visible,
            selected = visible.firstOrNull { it.id == selectedId },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExploreUiState())

    fun selectCategory(category: String?) {
        savedState[KeyCategory] = category
    }

    fun selectSpot(id: String) {
        savedState[KeySelected] = id
    }

    fun clearSelection() {
        savedState[KeySelected] = null
    }

    fun refreshLocation() = locationProvider.refresh()

    companion object {
        private const val KeyCategory = "category"
        private const val KeySelected = "selectedSpotId"

        val Factory = viewModelFactory {
            initializer {
                ExploreViewModel(AppGraph.foodSpotRepository, AppGraph.userLocationProvider, createSavedStateHandle())
            }
        }
    }
}
