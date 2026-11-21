package com.example.spot.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.spot.R
import com.example.spot.data.model.FoodSpot
import com.example.spot.di.AppGraph
import com.example.spot.ui.explore.ExploreMapRoute
import com.example.spot.ui.home.HomeRoute
import com.example.spot.ui.profile.AboutRoute
import com.example.spot.ui.profile.AppSettingsRoute
import com.example.spot.ui.profile.HelpRoute
import com.example.spot.ui.profile.ProfileRoute
import com.example.spot.ui.saved.SavedRoute
import com.example.spot.ui.search.SearchRoute
import com.example.spot.ui.screens.SubScreenPlaceholder
import kotlinx.coroutines.launch

/** Routes outside the bottom bar (the bar hides on these). */
object SpotRoutes {
    const val ExploreMap = "explore-map"
    const val Details = "details/{id}"
    const val AppSettings = "app-settings"
    const val About = "about"
    const val Help = "help"
    fun details(id: String) = "details/$id"

    /** Set on the Explore entry by Home's search field: focus the search field and open the keyboard. */
    const val FocusSearchKey = "focusSearchField"
}

@Composable
fun SpotNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    // Navigation Compose crossfades by default; screens switch instantly instead.
    NavHost(
        navController = navController,
        startDestination = SpotDestination.Home.route,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(SpotDestination.Home.route) {
            TabSurface {
                HomeRoute(
                    onSearchClick = {
                        navController.navigateToTab(SpotDestination.Explore)
                        navController.currentBackStackEntry?.savedStateHandle?.set(SpotRoutes.FocusSearchKey, true)
                    },
                    onProfileClick = { navController.navigateToTab(SpotDestination.Profile) },
                    onViewMapClick = { navController.navigate(SpotRoutes.ExploreMap) { launchSingleTop = true } },
                    onSeeAllClick = { navController.navigateToTab(SpotDestination.Explore) },
                    onSpotClick = { id -> navController.navigate(SpotRoutes.details(id)) { launchSingleTop = true } },
                )
            }
        }

        // The Explore tab is the Search screen.
        composable(SpotDestination.Explore.route) { entry ->
            val focusRequested by entry.savedStateHandle.getStateFlow(SpotRoutes.FocusSearchKey, false)
                .collectAsStateWithLifecycle()
            TabSurface {
                SearchRoute(
                    focusRequested = focusRequested,
                    onFocusRequestHandled = { entry.savedStateHandle[SpotRoutes.FocusSearchKey] = false },
                    onBack = { navController.navigateToTab(SpotDestination.Home) }, // saves the query for next time
                    onOpenDetails = { id -> navController.navigate(SpotRoutes.details(id)) { launchSingleTop = true } },
                )
            }
        }

        // The Saved tab is Saved Places.
        composable(SpotDestination.Saved.route) {
            TabSurface {
                SavedRoute(
                    onBack = { navController.navigateToTab(SpotDestination.Home) },
                    onOpenDetails = { id -> navController.navigate(SpotRoutes.details(id)) { launchSingleTop = true } },
                    onExplore = { navController.navigateToTab(SpotDestination.Explore) },
                )
            }
        }

        // The Profile tab and its three sub-screens (no bottom bar on those).
        composable(SpotDestination.Profile.route) {
            TabSurface {
                ProfileRoute(
                    onOpenSaved = { navController.navigateToTab(SpotDestination.Saved) },
                    onOpenSettings = { navController.navigate(SpotRoutes.AppSettings) { launchSingleTop = true } },
                    onOpenAbout = { navController.navigate(SpotRoutes.About) { launchSingleTop = true } },
                    onOpenHelp = { navController.navigate(SpotRoutes.Help) { launchSingleTop = true } },
                )
            }
        }
        composable(SpotRoutes.AppSettings) { TabSurface { AppSettingsRoute(onBack = { navController.popBackStack() }) } }
        composable(SpotRoutes.About) { TabSurface { AboutRoute(onBack = { navController.popBackStack() }) } }
        composable(SpotRoutes.Help) { TabSurface { HelpRoute(onBack = { navController.popBackStack() }) } }

        composable(SpotRoutes.ExploreMap) {
            TabSurface {
                ExploreMapRoute(
                    onBack = { navController.popBackStack() },
                    onOpenDetails = { id -> navController.navigate(SpotRoutes.details(id)) { launchSingleTop = true } },
                )
            }
        }

        composable(
            route = SpotRoutes.Details,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            val spot by produceState<FoodSpot?>(initialValue = null, id) {
                value = AppGraph.foodSpotRepository.getSpot(id)
            }
            // Minimal heart until the real Details screen lands; it uses the same shared repository.
            val savedFlow = remember(id) { AppGraph.savedSpotsRepository.isSaved(id) }
            val saved by savedFlow.collectAsStateWithLifecycle(initialValue = false)
            val scope = rememberCoroutineScope()
            TabSurface {
                SubScreenPlaceholder(
                    title = spot?.name.orEmpty(),
                    note = stringResource(R.string.details_placeholder_note),
                    onBack = { navController.popBackStack() },
                    actions = {
                        val name = spot?.name.orEmpty()
                        IconButton(onClick = { scope.launch { AppGraph.savedSpotsRepository.toggle(id) } }) {
                            Icon(
                                imageVector = if (saved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = stringResource(if (saved) R.string.saved_cd_unsave else R.string.saved_cd_save, name),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                )
            }
        }
    }
}

/**
 * Even with no transition, NavHost keeps the outgoing screen for one more frame while the
 * new one is drawn. An opaque background makes the new screen fully cover the old one,
 * so the two never show through each other.
 */
@Composable
private fun TabSurface(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        content()
    }
}

/** Standard bottom-nav behaviour: one copy per tab, each tab's state saved and restored. */
fun NavHostController.navigateToTab(destination: SpotDestination) {
    navigate(destination.route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
