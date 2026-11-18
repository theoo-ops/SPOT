package com.example.spot.location

import com.example.spot.data.model.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

sealed interface LocationState {
    /** Permission granted, waiting for the first fix. */
    data object Loading : LocationState

    /**
     * @param isPrecise false when the user granted only approximate location. Approximate fixes
     * can be off by a kilometre or more, so distances are hidden then.
     */
    data class Available(
        val latitude: Double,
        val longitude: Double,
        val accuracyMeters: Float,
        val isPrecise: Boolean,
    ) : LocationState {
        val point: GeoPoint get() = GeoPoint(latitude, longitude)
    }

    /** No location permission (never asked, denied, or "don't ask again"). */
    data object PermissionDenied : LocationState

    /** Permission granted but the phone's location services are off. */
    data object Unavailable : LocationState
}

/** Only an Available, precise fix is trustworthy enough for "200 m away". */
val LocationState.distanceOrigin: GeoPoint?
    get() = (this as? LocationState.Available)?.takeIf { it.isPrecise }?.point

interface UserLocationProvider {
    val state: StateFlow<LocationState>

    /** Re-check permission and location services (after a permission result or returning from Settings). */
    fun refresh()
}

/** Fixed location for previews and tests. */
class FakeUserLocationProvider(
    initial: LocationState = LocationState.Available(
        SpotGeo.ReferencePoint.latitude, SpotGeo.ReferencePoint.longitude,
        accuracyMeters = 15f, isPrecise = true,
    ),
) : UserLocationProvider {
    override val state = MutableStateFlow(initial)
    override fun refresh() = Unit
}

object SpotGeo {
    /** PLACEHOLDER for Golden Country Homes, near BatStateU Alangilan. Replace with the real point. */
    val ReferencePoint = GeoPoint(latitude = 13.7840, longitude = 121.0740)
}

/** The one place distances are computed and formatted. */
object Distance {
    private const val EarthRadiusMeters = 6_371_000.0

    /** Great-circle (haversine) distance in meters. */
    fun meters(from: GeoPoint, to: GeoPoint): Double {
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(from.latitude)) * cos(Math.toRadians(to.latitude)) *
            sin(dLng / 2) * sin(dLng / 2)
        return 2 * EarthRadiusMeters * asin(sqrt(a))
    }

    /**
     * "200 m away" under 1 km (nearest 10 m, never "0 m"), "1.2 km away" from 1 km, "120 km away" from 100 km.
     * Non-breaking spaces keep the label on one line when it wraps (no orphaned "away").
     */
    fun format(meters: Double): String {
        val roundedMeters = ((meters / 10).roundToInt() * 10).coerceAtLeast(10)
        val km = meters / 1000
        return when {
            roundedMeters < 1000 -> "$roundedMeters${Nbsp}m${Nbsp}away"
            km < 100 -> String.format(Locale.US, "%.1f${Nbsp}km${Nbsp}away", km)
            else -> "${km.roundToInt()}${Nbsp}km${Nbsp}away"
        }
    }

    /** Distance label from [origin], or null when there is no trustworthy origin. */
    fun labelOrNull(origin: GeoPoint?, to: GeoPoint): String? = origin?.let { format(meters(it, to)) }

    private const val Nbsp = ' '
}
