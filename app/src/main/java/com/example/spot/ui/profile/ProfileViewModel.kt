package com.example.spot.ui.profile

import androidx.annotation.PluralsRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.spot.R
import com.example.spot.data.FoodSpotRepository
import com.example.spot.data.profile.ProfileName
import com.example.spot.data.profile.ProfileRepository
import com.example.spot.data.saved.SavedSpotsRepository
import com.example.spot.data.saved.existingSavedSpots
import com.example.spot.di.AppGraph
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One cell of the stats card. Add more to [ProfileUiState.stats] later (Visits, Reviews...). */
@Immutable
data class ProfileStat(
    val value: Int,
    /** Plural label: "Favorite"/"Favorites" on screen, and the spoken "3 favorites". */
    @param:PluralsRes val label: Int,
    @param:PluralsRes val spoken: Int,
)

@Immutable
data class ProfileUiState(
    val name: String = ProfileName.Guest,
    val handle: String = ProfileName.handle(ProfileName.Guest),
    val initials: String = ProfileName.initials(ProfileName.Guest),
    val isGuest: Boolean = true,
    val stats: List<ProfileStat> = listOf(ProfileStat(0, R.plurals.profile_stat_favorites, R.plurals.profile_stat_favorites_spoken)),
)

class ProfileViewModel(
    private val profile: ProfileRepository,
    savedSpots: SavedSpotsRepository,
    foodSpots: FoodSpotRepository,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        profile.displayName,
        savedSpots.existingSavedSpots(foodSpots), // same flow as Saved Places, so the counts match
    ) { name, saved ->
        ProfileUiState(
            name = name,
            handle = ProfileName.handle(name),
            initials = ProfileName.initials(name),
            isGuest = name == ProfileName.Guest,
            stats = listOf(ProfileStat(saved.size, R.plurals.profile_stat_favorites, R.plurals.profile_stat_favorites_spoken)),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun saveName(name: String) {
        viewModelScope.launch { profile.setDisplayName(name) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { ProfileViewModel(AppGraph.profileRepository, AppGraph.savedSpotsRepository, AppGraph.foodSpotRepository) }
        }
    }
}
