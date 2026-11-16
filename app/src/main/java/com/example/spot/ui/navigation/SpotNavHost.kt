package com.example.spot.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.spot.R
import com.example.spot.ui.screens.PlaceholderScreen

@Composable
fun SpotNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    // Navigation Compose crossfades by default; tabs switch instantly instead.
    NavHost(
        navController = navController,
        startDestination = SpotDestination.Home.route,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        SpotDestination.entries.forEach { destination ->
            composable(destination.route) {
                TabSurface {
                    PlaceholderScreen(
                        title = stringResource(destination.labelRes),
                        note = stringResource(R.string.placeholder_note),
                    )
                }
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
