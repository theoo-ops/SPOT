package com.example.spot.data.fake

import com.example.spot.data.model.FoodSpot
import com.example.spot.data.saved.SavedEntry
import com.example.spot.location.SpotGeo

/**
 * break-ui worst-case data for trying screens on a device. Off by default;
 * flip [DebugData.Enabled] (dev builds only, never ship it on).
 */
object DebugData {
    const val Enabled = false

    fun spotsOr(normal: List<FoodSpot>): List<FoodSpot> = if (Enabled) WorstCaseSpots + manySpots(1_000) else normal

    /**
     * Saved spots for the debug data set: the worst cases (newest), a saved ID that no longer
     * exists in the data (must be skipped), then 500 more.
     */
    fun savedEntries(now: Long = System.currentTimeMillis()): List<SavedEntry> =
        (WorstCaseSpots.map { it.id } + "deleted-spot-404" + manySpots(500).map { it.id })
            .mapIndexed { i, id -> SavedEntry(id, now - i * 60_000L) }
}

private fun spotNorthOfReference(
    id: String, name: String, category: String, rating: Double?, meters: Double,
) = FoodSpot(
    id = id, name = name, category = category, rating = rating,
    latitude = SpotGeo.ReferencePoint.latitude + meters / 110_574.0,
    longitude = SpotGeo.ReferencePoint.longitude,
    address = "Alangilan, Batangas City", hours = "8:00 AM – 9:00 PM",
)

/** Realistic worst cases: long, short, accented, emoji, unrated, far away. */
val WorstCaseSpots = listOf(
    // 60 characters, no rating, no photo
    spotNorthOfReference("worst-long", "Kusina ni Aling Rosario at Kakanin sa Kanto — Golden Country", "Karinderya", null, 8.0),
    spotNorthOfReference("worst-short", "Jo's", "Café", 5.0, 995.0),
    spotNorthOfReference("worst-accents", "Bánh Mì & Phở Corner", "Vietnamese", 4.25, 1_050.0),
    spotNorthOfReference("worst-emoji", "Kape ☕ at Pandesal ni Mang Totoy", "Bakery", 3.9, 2_400.0),
    spotNorthOfReference("worst-symbols", "100% Pure Ube (Halo-Halo) #1 & More!", "Dessert", 4.95, 12_449.0),
)

/** For scrolling and performance: N plain spots spread out north of the reference point. */
fun manySpots(count: Int) = List(count) { i ->
    spotNorthOfReference(
        id = "many-$i", name = "Food Spot #${i + 1}",
        category = listOf("Filipino", "Karinderya", "Café", "Pizza", "Milk Tea", "Grill")[i % 6],
        rating = if (i % 7 == 0) null else 3.0 + (i % 20) / 10.0, meters = 100.0 + i * 25,
    )
}
