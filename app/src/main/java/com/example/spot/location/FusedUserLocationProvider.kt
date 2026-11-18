package com.example.spot.location

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Real location via FusedLocationProviderClient. Never asks for permission itself; the UI does,
 * then calls [refresh]. Updates run only while someone collects [state] (+5 s grace).
 */
class FusedUserLocationProvider(
    context: Context,
    scope: CoroutineScope,
) : UserLocationProvider {

    private val context = context.applicationContext
    private val client = LocationServices.getFusedLocationProviderClient(this.context)
    private val locationManager = this.context.getSystemService(LocationManager::class.java)
    private val refreshes = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<LocationState> =
        combine(refreshes, locationSettingsChanges()) { _, _ -> Unit }
            .flatMapLatest { currentState() }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), LocationState.Loading)

    override fun refresh() = refreshes.update { it + 1 }

    private fun currentState(): Flow<LocationState> = when {
        !LocationPermissions.isGranted(context) -> flowOf(LocationState.PermissionDenied)
        !LocationManagerCompat.isLocationEnabled(locationManager) -> flowOf(LocationState.Unavailable)
        else -> updates(precise = LocationPermissions.isPrecise(context))
            .onStart { emit(LocationState.Loading) }
    }

    @SuppressLint("MissingPermission") // checked in currentState(); SecurityException still handled
    private fun updates(precise: Boolean): Flow<LocationState> = callbackFlow {
        fun send(location: android.location.Location) {
            trySend(LocationState.Available(location.latitude, location.longitude, location.accuracy, precise))
        }

        val request = LocationRequest.Builder(
            if (precise) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            10_000L,
        ).setMinUpdateIntervalMillis(5_000L).setMinUpdateDistanceMeters(10f).build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let(::send)
            }
        }
        try {
            client.lastLocation.addOnSuccessListener { it?.let(::send) } // fast first fix if one is cached
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } catch (_: SecurityException) {
            trySend(LocationState.PermissionDenied)
        }
        awaitClose { client.removeLocationUpdates(callback) }
    }

    /** Emits once at start, then whenever the user toggles location services. */
    private fun locationSettingsChanges(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(Unit)
            }
        }
        ContextCompat.registerReceiver(
            context, receiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        trySend(Unit)
        awaitClose { context.unregisterReceiver(receiver) }
    }
}
