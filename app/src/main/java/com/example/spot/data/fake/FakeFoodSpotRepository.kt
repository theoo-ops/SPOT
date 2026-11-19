package com.example.spot.data.fake

import com.example.spot.data.FoodSpotRepository
import com.example.spot.data.SearchMatching
import com.example.spot.data.model.FoodSpot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * In-memory stand-in for the real backend.
 * @param latencyMs simulated network delay; raise it (e.g. 1500) to see Home's loading state.
 */
class FakeFoodSpotRepository(
    private val spots: List<FoodSpot> = SeedFoodSpots,
    private val latencyMs: Long = 0,
) : FoodSpotRepository {

    override fun observeSpots(): Flow<List<FoodSpot>> = flow {
        if (latencyMs > 0) delay(latencyMs)
        emit(spots)
    }

    override suspend fun getSpot(id: String): FoodSpot? = spots.firstOrNull { it.id == id }

    override suspend fun search(query: String, category: String?): List<FoodSpot> {
        if (latencyMs > 0) delay(latencyMs)
        return spots.filter { (category == null || it.category == category) && SearchMatching.matches(it, query) }
    }
}

/**
 * PLACEHOLDER coordinates, laid out around FakeUserLocationProvider.DefaultPoint
 * (13.7840, 121.0740) so the distances come out as in the mockup. Replace with real ones.
 */
val SeedFoodSpots = listOf(
    FoodSpot(
        id = "mang-juans-carinderia", name = "Mang Juan's Carinderia", category = "Filipino", rating = 4.6,
        latitude = 13.78581, longitude = 121.07400, // ~200 m N
        address = "Golden Country Homes, BatStateU Alangilan", hours = "8:00 AM – 9:00 PM",
    ),
    FoodSpot(
        id = "cafe-dulce", name = "Café Dulce", category = "Café", rating = 4.8,
        latitude = 13.78400, longitude = 121.07724, // ~350 m E
        address = "Alangilan, Batangas City", hours = "7:00 AM – 10:00 PM",
    ),
    FoodSpot(
        id = "pizza-house", name = "Pizza House", category = "Pizza", rating = 4.7,
        latitude = 13.78129, longitude = 121.07400, // ~300 m S
        address = "Alangilan, Batangas City", hours = "10:00 AM – 10:00 PM",
    ),
    FoodSpot(
        id = "slice-and-more", name = "Slice & More", category = "Pizza", rating = 4.5,
        latitude = 13.78400, longitude = 121.06984, // ~450 m W
        address = "Alangilan, Batangas City", hours = "11:00 AM – 9:00 PM",
    ),
    FoodSpot(
        id = "ricos-pizza", name = "Rico's Pizza", category = "Pizza", rating = 4.4,
        latitude = 13.78720, longitude = 121.07727, // ~500 m NE
        address = "Alangilan, Batangas City", hours = "11:00 AM – 11:00 PM",
    ),
    // Invented
    FoodSpot(
        id = "lola-nenas", name = "Lola Nena's Lutong Bahay", category = "Karinderya", rating = 4.3,
        latitude = 13.77984, longitude = 121.06975, // ~650 m SW
        address = "Alangilan, Batangas City", hours = "6:00 AM – 2:00 PM",
    ),
    FoodSpot(
        id = "tea-haus", name = "Tea Haus Alangilan", category = "Milk Tea", rating = 4.2,
        latitude = 13.79214, longitude = 121.07400, // ~900 m N
        address = "Alangilan, Batangas City", hours = "12:00 PM – 10:00 PM",
    ),
    FoodSpot(
        id = "ihaw-ihaw", name = "Ihaw-Ihaw sa Kanto", category = "Grill", rating = 4.1,
        latitude = 13.78400, longitude = 121.08510, // ~1.2 km E
        address = "Alangilan, Batangas City", hours = "4:00 PM – 12:00 AM",
    ),
)
