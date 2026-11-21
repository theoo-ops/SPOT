package com.example.spot.data.saved

import com.example.spot.data.FoodSpotRepository
import com.example.spot.data.model.FoodSpot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** One saved spot and when it was saved (epoch millis). */
data class SavedEntry(val spotId: String, val savedAt: Long)

interface SavedSpotsRepository {
    /** Every saved spot, newest saved first. Shared by Details (heart) and Saved Places. */
    val saved: Flow<List<SavedEntry>>

    fun isSaved(spotId: String): Flow<Boolean> = saved.map { list -> list.any { it.spotId == spotId } }.distinctUntilChanged()

    /** Saves with the current time; a spot that's already saved keeps its original time. */
    suspend fun save(spotId: String)

    suspend fun remove(spotId: String)

    /** Puts a removed spot back with its original time, so it returns to the same place (Undo). */
    suspend fun restore(spotId: String, savedAt: Long)

    suspend fun toggle(spotId: String) {
        if (isSavedNow(spotId)) remove(spotId) else save(spotId)
    }

    suspend fun isSavedNow(spotId: String): Boolean
}

/**
 * Saved entries joined with the spot data, newest first. Saved IDs that no longer exist in the data
 * are skipped. Saved Places and the Profile "Favorites" count both use this, so their numbers match.
 */
fun SavedSpotsRepository.existingSavedSpots(foodSpots: FoodSpotRepository): Flow<List<Pair<SavedEntry, FoodSpot>>> =
    combine(saved, foodSpots.observeSpots()) { entries, spots ->
        val byId = spots.associateBy { it.id }
        entries.mapNotNull { entry -> byId[entry.spotId]?.let { entry to it } }
    }

/** Newest first; ties (e.g. migrated IDs with equal times) by id so the order is stable. */
internal fun Collection<SavedEntry>.newestFirst() =
    sortedWith(compareByDescending<SavedEntry> { it.savedAt }.thenBy { it.spotId })

/** In memory: previews, tests and the debug data set. */
class InMemorySavedSpotsRepository(
    initial: List<SavedEntry> = emptyList(),
    private val now: () -> Long = System::currentTimeMillis,
) : SavedSpotsRepository {
    private val entries = MutableStateFlow(initial.associate { it.spotId to it.savedAt })

    override val saved: Flow<List<SavedEntry>> =
        entries.map { map -> map.map { (id, at) -> SavedEntry(id, at) }.newestFirst() }

    override suspend fun save(spotId: String) = entries.update { if (spotId in it) it else it + (spotId to now()) }
    override suspend fun remove(spotId: String) = entries.update { it - spotId }
    override suspend fun restore(spotId: String, savedAt: Long) = entries.update { it + (spotId to savedAt) }
    override suspend fun isSavedNow(spotId: String) = spotId in entries.value
}
