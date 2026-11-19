package com.example.spot.data

import com.example.spot.data.model.FoodSpot
import kotlinx.coroutines.flow.Flow

interface FoodSpotRepository {
    /** Every spot. Emits again whenever the data changes. */
    fun observeSpots(): Flow<List<FoodSpot>>

    suspend fun getSpot(id: String): FoodSpot?

    /**
     * Spots whose name or category matches [query] (case- and accent-insensitive, see
     * [SearchMatching]), within [category] when it isn't null. An empty query returns every spot.
     * Unsorted: the caller orders by distance or rating.
     */
    suspend fun search(query: String, category: String?): List<FoodSpot>
}

object FoodCategories {
    /**
     * Chip order on Home. A category may have no spots yet (Bakery in the seed data);
     * Home shows its empty state for it.
     */
    val all = listOf("Filipino", "Karinderya", "Café", "Pizza", "Milk Tea", "Grill", "Bakery")
}
