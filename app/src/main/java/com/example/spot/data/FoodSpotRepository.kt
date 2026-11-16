package com.example.spot.data

import com.example.spot.data.model.FoodSpot
import kotlinx.coroutines.flow.Flow

interface FoodSpotRepository {
    /** Every spot. Emits again whenever the data changes. */
    fun observeSpots(): Flow<List<FoodSpot>>

    suspend fun getSpot(id: String): FoodSpot?
}

object FoodCategories {
    /**
     * Chip order on Home. A category may have no spots yet (Bakery in the seed data);
     * Home shows its empty state for it.
     */
    val all = listOf("Filipino", "Karinderya", "Café", "Pizza", "Milk Tea", "Grill", "Bakery")
}
