package com.example.spot.ui.saved

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.spot.data.FoodSpotRepository
import com.example.spot.data.saved.SavedSpotsRepository
import com.example.spot.data.saved.existingSavedSpots
import com.example.spot.di.AppGraph
import com.example.spot.location.Distance
import com.example.spot.location.UserLocationProvider
import com.example.spot.location.distanceOrigin
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class SavedItem(
    val id: String,
    val name: String,
    @param:DrawableRes val photo: Int?,
    /** Null when location is unavailable: the card shows the name only. */
    val distanceLabel: String?,
    /** Kept so Undo can put the spot back in its original place. */
    val savedAt: Long,
)

@Immutable
data class SavedUiState(
    val isLoading: Boolean = true,
    /** Newest saved first. Saved IDs that no longer exist in the data are skipped. */
    val items: List<SavedItem> = emptyList(),
)

class SavedViewModel(
    private val savedSpots: SavedSpotsRepository,
    foodSpots: FoodSpotRepository,
    locationProvider: UserLocationProvider,
) : ViewModel() {

    val uiState: StateFlow<SavedUiState> = combine(
        savedSpots.existingSavedSpots(foodSpots), // skips saved IDs that no longer exist; same flow as Profile's count
        locationProvider.state,
    ) { saved, location ->
        val origin = location.distanceOrigin
        SavedUiState(
            isLoading = false,
            items = saved.map { (entry, spot) ->
                SavedItem(spot.id, spot.name, spot.photo, Distance.labelOrNull(origin, spot.location), entry.savedAt)
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavedUiState())

    /** Removes right away (no confirmation); the screen offers Undo. */
    fun remove(item: SavedItem) {
        viewModelScope.launch { savedSpots.remove(item.id) }
    }

    fun undoRemove(item: SavedItem) {
        viewModelScope.launch { savedSpots.restore(item.id, item.savedAt) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                SavedViewModel(AppGraph.savedSpotsRepository, AppGraph.foodSpotRepository, AppGraph.userLocationProvider)
            }
        }
    }
}
