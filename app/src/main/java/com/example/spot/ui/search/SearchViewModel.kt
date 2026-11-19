package com.example.spot.ui.search

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
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
import com.example.spot.ui.home.SpotItem
import com.example.spot.ui.home.toItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@Immutable
data class SearchUiState(
    val isLoading: Boolean = true,
    /** The query these results are for (the field itself reads [SearchViewModel.query]). */
    val query: String = "",
    val categories: List<String> = FoodCategories.all,
    val selectedCategory: String? = null,
    /** Nearest first when distances are known, otherwise highest rated first. */
    val results: List<SpotItem> = emptyList(),
)

class SearchViewModel(
    private val repository: FoodSpotRepository,
    locationProvider: UserLocationProvider,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    /**
     * Compose state, written synchronously while typing (a StateFlow round trip can make the
     * cursor jump), mirrored into SavedStateHandle so it survives rotation and process death.
     */
    var query by mutableStateOf(savedState.get<String>(KeyQuery).orEmpty())
        private set

    private val categoryFlow = savedState.getStateFlow<String?>(KeyCategory, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SearchUiState> = combine(snapshotFlow { query }, categoryFlow) { q, c -> q to c }
        .mapLatest { (q, c) -> Triple(q, c, repository.search(q, c)) } // newest keystroke wins
        .combine(locationProvider.state) { (q, c, spots), location ->
            val origin = location.distanceOrigin
            val results = if (origin != null) {
                spots.map { it to Distance.meters(origin, it.location) }
                    .sortedBy { (_, meters) -> meters }
                    .map { (spot, meters) -> spot.toItem(Distance.format(meters)) }
            } else {
                spots.sortedWith(compareByDescending<FoodSpot> { it.rating ?: -1.0 }.thenBy { it.name }) // unrated last
                    .map { it.toItem(distanceLabel = null) }
            }
            SearchUiState(isLoading = false, query = q, selectedCategory = c, results = results)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState(query = query))

    fun onQueryChange(value: String) {
        query = value
        savedState[KeyQuery] = value
    }

    fun clearQuery() = onQueryChange("")

    fun selectCategory(category: String?) {
        savedState[KeyCategory] = category
    }

    companion object {
        private const val KeyQuery = "query"
        private const val KeyCategory = "category"

        val Factory = viewModelFactory {
            initializer { SearchViewModel(AppGraph.foodSpotRepository, AppGraph.userLocationProvider, createSavedStateHandle()) }
        }
    }
}
