package com.example.spot.data

import com.example.spot.data.fake.FakeFoodSpotRepository
import com.example.spot.data.fake.SeedFoodSpots
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchMatchingTest {
    private val repo = FakeFoodSpotRepository(SeedFoodSpots)
    private fun names(query: String, category: String? = null) = runBlocking { repo.search(query, category).map { it.name } }

    @Test fun accentsAreIgnored() {
        assertEquals(listOf("Café Dulce"), names("cafe dulce"))
        assertTrue("Café Dulce" in names("CAFÉ"))
    }

    @Test fun caseAndSpacesAreIgnored() = assertEquals(names("pizza"), names("   PiZzA  "))

    @Test fun matchesNameOrCategoryInAnyWordOrder() {
        assertEquals(listOf("Pizza House", "Slice & More", "Rico's Pizza"), names("pizza"))
        assertEquals(listOf("Pizza House"), names("house   pizza"))
    }

    @Test fun emptyQueryReturnsEverything() = assertEquals(SeedFoodSpots.size, names("").size)

    @Test fun categoryFiltersTheResult() = assertEquals(listOf("Slice & More"), names("slice", category = "Pizza"))

    @Test fun symbolsAndEmojiJustDontMatch() = assertEquals(emptyList<String>(), names("🍕 #1 (100%)"))
}
