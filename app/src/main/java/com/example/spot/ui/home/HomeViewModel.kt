package com.example.spot.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.spot.data.FoodCategories
import com.example.spot.data.FoodSpotRepository
import com.example.spot.data.model.FoodSpot
import com.example.spot.di.AppGraph
import com.example.spot.location.Distance
import com.example.spot.location.UserLocationProvider
import com.example.spot.location.distanceOrigin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalTime

enum class Greeting {
    Morning, Afternoon, Evening;

    companion object {
        fun at(time: LocalTime): Greeting = when (time.hour) {
            in 5..11 -> Morning
            in 12..17 -> Afternoon
            else -> Evening
        }
    }
}

@Immutable
data class SpotItem(
    val id: String,
    val name: String,
    val category: String,
    val rating: Double?,
    /** Null when there is no trustworthy location: the card shows the category only. */
    val distanceLabel: String?,
    @param:DrawableRes val photo: Int?,
)

@Immutable
data class HomeUiState(
    val isLoading: Boolean = true,
    val greeting: Greeting = Greeting.Morning,
    val categories: List<String> = FoodCategories.all,
    /** null = "All". */
    val selectedCategory: String? = null,
    /** Filtered by [selectedCategory]; nearest first when distances are known, else repository order. */
    val spots: List<SpotItem> = emptyList(),
)

class HomeViewModel(
    repository: FoodSpotRepository,
    locationProvider: UserLocationProvider,
    private val clock: () -> LocalTime = { LocalTime.now() },
) : ViewModel() {

    private val selectedCategory = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeSpots(),
        locationProvider.state,
        selectedCategory,
    ) { spots, location, selected ->
        val origin = location.distanceOrigin // null unless precise + available: hide distances
        val filtered = spots.filter { selected == null || it.category == selected }
        HomeUiState(
            isLoading = false,
            greeting = Greeting.at(clock()),
            selectedCategory = selected,
            spots = if (origin == null) {
                filtered.map { it.toItem(distanceLabel = null) }
            } else {
                filtered
                    .map { it to Distance.meters(origin, it.location) }
                    .sortedBy { (_, meters) -> meters }
                    .map { (spot, meters) -> spot.toItem(Distance.format(meters)) }
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(greeting = Greeting.at(clock())),
    )

    /** null selects "All". */
    fun selectCategory(category: String?) {
        selectedCategory.value = category
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { HomeViewModel(AppGraph.foodSpotRepository, AppGraph.userLocationProvider) }
        }
    }
}

internal fun FoodSpot.toItem(distanceLabel: String?) = SpotItem(
    id = id, name = name, category = category, rating = rating,
    distanceLabel = distanceLabel, photo = photo,
)
