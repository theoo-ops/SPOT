package com.example.spot.ui.explore

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

internal const val MapLogTag = "SPOT/Map"

/** Logs a clear message when the Maps key is missing; the map would just be blank otherwise. */
internal fun logIfMapsKeyMissing(context: Context) {
    val pm = context.packageManager
    val info = if (Build.VERSION.SDK_INT >= 33) {
        pm.getApplicationInfo(context.packageName, PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
    } else {
        @Suppress("DEPRECATION")
        pm.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
    }
    val key = info.metaData?.getString("com.google.android.geo.API_KEY")
    if (key.isNullOrBlank() || key == "DEFAULT_API_KEY") {
        Log.e(
            MapLogTag,
            "No Google Maps API key: the map will be blank. Add MAPS_API_KEY=<your key> to " +
                "local.properties (project root), then rebuild.",
        )
    }
}

internal const val MapLoadTimeoutMs = 10_000L

internal fun logMapDidNotLoad() {
    Log.e(
        MapLogTag,
        "Map tiles didn't load within ${MapLoadTimeoutMs / 1000} s. Likely causes: an invalid or restricted API key " +
            "(check the key's Android app restriction: package com.example.spot + your SHA-1, and that " +
            "'Maps SDK for Android' is enabled), or no network. Look for 'Authorization failure' in Logcat.",
    )
}

/** True while the device has an internet-capable network. */
@Composable
internal fun rememberIsOnline(): State<Boolean> {
    val context = LocalContext.current
    val cm = remember(context) { context.getSystemService(ConnectivityManager::class.java) }
    fun isOnlineNow() = cm.getNetworkCapabilities(cm.activeNetwork)
        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    val online = remember { mutableStateOf(isOnlineNow()) }
    DisposableEffect(cm) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { online.value = true }
            override fun onLost(network: Network) { online.value = isOnlineNow() }
        }
        cm.registerDefaultNetworkCallback(callback, Handler(Looper.getMainLooper()))
        onDispose { cm.unregisterNetworkCallback(callback) }
    }
    return online
}
