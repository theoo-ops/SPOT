package com.example.spot.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spot.ui.navigation.SpotBottomBar
import com.example.spot.ui.navigation.SpotDestination
import com.example.spot.ui.navigation.SpotNavHost
import com.example.spot.ui.navigation.SpotRoutes
import com.example.spot.ui.navigation.navigateToTab
import com.example.spot.ui.theme.SpotTheme

@Composable
fun SpotApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = SpotDestination.entries.firstOrNull { tab ->
        backStackEntry?.destination?.hierarchy?.any { it.route == tab.route } == true
    }?.route

    Scaffold(
        bottomBar = {
            // Only on the four tabs; Explore Map and Details are full screen.
            if (currentRoute != null) {
                SpotBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = navController::navigateToTab,
                )
            }
        },
    ) { innerPadding ->
        // consumeWindowInsets: screens that add imePadding() (Search) subtract the bottom bar they
        // already clear, so only their content moves up and the bar stays behind the keyboard.
        // Details is edge to edge (photo behind the status bar) and handles its own insets.
        val padding = if (backStackEntry?.destination?.route == SpotRoutes.Details) PaddingValues() else innerPadding
        SpotNavHost(navController = navController, modifier = Modifier.padding(padding).consumeWindowInsets(padding))
    }
}

@Preview(name = "Default", showBackground = true, widthDp = 360, heightDp = 640)
@Preview(name = "Font 200%", showBackground = true, widthDp = 360, heightDp = 640, fontScale = 2f)
@Composable
private fun SpotAppPreview() {
    SpotTheme { SpotApp() }
}
