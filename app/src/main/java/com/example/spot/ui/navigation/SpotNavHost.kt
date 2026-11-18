package com.example.spot.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.example.spot.ui.screens.PlaceholderScreen
import com.example.spot.ui.screens.SubScreenPlaceholder

/** Routes outside the bottom bar (the bar hides on these). */
object SpotRoutes {
    const val Search = "search"
    const val ExploreMap = "explore-map"
    const val Details = "details/{id}"
    fun details(id: String) = "details/$id"
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
                    onSearchClick = { navController.navigate(SpotRoutes.Search) { launchSingleTop = true } },
                    onProfileClick = { navController.navigateToTab(SpotDestination.Profile) },
                    onViewMapClick = { navController.navigate(SpotRoutes.ExploreMap) { launchSingleTop = true } },
                    onSeeAllClick = { navController.navigateToTab(SpotDestination.Explore) },
                    onSpotClick = { id -> navController.navigate(SpotRoutes.details(id)) { launchSingleTop = true } },
                )
            }
        }

        SpotDestination.entries.filter { it != SpotDestination.Home }.forEach { destination ->
            composable(destination.route) {
                TabSurface {
                    PlaceholderScreen(
                        title = stringResource(destination.labelRes),
                        note = stringResource(R.string.placeholder_note),
                    )
                }
            }
        }

        composable(SpotRoutes.ExploreMap) {
            TabSurface {
                ExploreMapRoute(
                    onBack = { navController.popBackStack() },
                    onOpenDetails = { id -> navController.navigate(SpotRoutes.details(id)) { launchSingleTop = true } },
                )
            }
        }

        composable(SpotRoutes.Search) {
            TabSurface {
                SubScreenPlaceholder(
                    title = stringResource(R.string.search_title),
                    note = stringResource(R.string.placeholder_note),
                    onBack = { navController.popBackStack() },
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
            TabSurface {
                SubScreenPlaceholder(
                    title = spot?.name.orEmpty(),
                    note = stringResource(R.string.details_placeholder_note),
                    onBack = { navController.popBackStack() },
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
