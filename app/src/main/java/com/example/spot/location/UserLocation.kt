package com.example.spot.location

import com.example.spot.data.model.GeoPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

interface UserLocationProvider {
    /** The user's location. A real provider emits again as it changes. */
    fun observeLocation(): Flow<GeoPoint>
}

class FakeUserLocationProvider(
    private val point: GeoPoint = DefaultPoint,
) : UserLocationProvider {
    override fun observeLocation(): Flow<GeoPoint> = flowOf(point)

    companion object {
        /** PLACEHOLDER near BatStateU Alangilan, Batangas City. Replace with your real point. */
        val DefaultPoint = GeoPoint(latitude = 13.7840, longitude = 121.0740)
    }
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

    private const val Nbsp = '\u00A0'
}
