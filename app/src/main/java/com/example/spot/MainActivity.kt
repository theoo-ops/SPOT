package com.example.spot

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.spot.ui.SpotRoot
import com.example.spot.ui.theme.SpotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate(): swaps Theme.SPOT.Starting for Theme.SPOT.
        val systemSplash = installSplashScreen()
        // The Compose splash starts on the same burgundy, so remove the system splash at once
        // instead of letting its default fade cover the pin's first frames.
        systemSplash.setOnExitAnimationListener { it.remove() }

        // A fresh launch shows the burgundy splash: start with white bar icons.
        // A restored activity (rotation, process death) skips the splash: start with dark ones.
        val bars = if (savedInstanceState == null) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)

        super.onCreate(savedInstanceState)
        setContent {
            SpotTheme { SpotRoot() }
        }
    }
}
