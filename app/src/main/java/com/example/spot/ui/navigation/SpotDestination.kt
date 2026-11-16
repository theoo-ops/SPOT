package com.example.spot.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.spot.R

enum class SpotDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Home("home", R.string.tab_home, Icons.Filled.Home, Icons.Outlined.Home),
    Explore("explore", R.string.tab_explore, Icons.Filled.Search, Icons.Outlined.Search),
    Saved("saved", R.string.tab_saved, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    Profile("profile", R.string.tab_profile, Icons.Filled.Person, Icons.Outlined.Person),
}
