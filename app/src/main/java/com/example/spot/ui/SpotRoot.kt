package com.example.spot.ui

import android.animation.ValueAnimator
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.spot.ui.motion.LocalAnimationsEnabled
import com.example.spot.ui.splash.SplashMotion
import com.example.spot.ui.splash.SpotSplash
import kotlinx.coroutines.delay

/**
 * Home is composed underneath from the start; the splash is an overlay that fades away.
 * It is not a nav destination, so Back can never return to it and the bottom bar needs no hiding.
 */
@Composable
fun SpotRoot() {
    val animate = remember { ValueAnimator.areAnimatorsEnabled() }
    var showSplash by rememberSaveable { mutableStateOf(true) }  // rotation doesn't replay it

    SystemBarIcons(lightIcons = showSplash, animate = animate)

    CompositionLocalProvider(LocalAnimationsEnabled provides animate) {
        Box(Modifier.fillMaxSize()) {
            SpotApp()
            AnimatedVisibility(
                visible = showSplash,
                enter = EnterTransition.None,
                exit = if (animate) {
                    fadeOut(tween(SplashMotion.ExitMs, easing = SplashMotion.EaseOut))
                } else {
                    ExitTransition.None
                },
            ) {
                SpotSplash(
                    animate = animate,
                    dataReady = true,  // later: your startup data state
                    onFinished = { showSplash = false },
                )
            }
        }
    }
}

/** White bar icons on the burgundy splash, dark ones on Home. */
@Composable
private fun SystemBarIcons(lightIcons: Boolean, animate: Boolean) {
    val activity = LocalActivity.current ?: return
    val view = LocalView.current
    LaunchedEffect(lightIcons) {
        if (!lightIcons && animate) delay(SplashMotion.BarIconsSwitchDelayMs.toLong())
        WindowCompat.getInsetsController(activity.window, view).apply {
            isAppearanceLightStatusBars = !lightIcons
            isAppearanceLightNavigationBars = !lightIcons
        }
    }
}
