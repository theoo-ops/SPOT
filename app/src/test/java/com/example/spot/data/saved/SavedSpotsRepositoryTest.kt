package com.example.spot.data.saved

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SavedSpotsRepositoryTest {
    @get:Rule val folder = TemporaryFolder()
    private val scopes = mutableListOf<CoroutineScope>()

    private fun store(name: String = "saved.preferences_pb", migrate: Boolean = true): Pair<DataStore<Preferences>, CoroutineScope> {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob()).also { scopes += it }
        val ds = PreferenceDataStoreFactory.create(
            migrations = if (migrate) listOf(LegacySavedIdsMigration) else emptyList(),
            scope = scope,
            produceFile = { folder.root.resolve(name) },
        )
        return ds to scope
    }

    @After fun tearDown() = scopes.forEach { it.cancel() }

    @Test fun newestSavedFirst() = runBlocking {
        var clock = 100L
        val repo = DataStoreSavedSpotsRepository(store().first) { clock++ }
        repo.save("a"); repo.save("b"); repo.save("c")
        assertEquals(listOf("c", "b", "a"), repo.saved.first().map { it.spotId })
    }

    @Test fun savingAgainKeepsTheOriginalTime() = runBlocking {
        var clock = 100L
        val repo = DataStoreSavedSpotsRepository(store().first) { clock++ }
        repo.save("a"); repo.save("b"); repo.save("a")
        assertEquals(listOf("b", "a"), repo.saved.first().map { it.spotId })
    }

    @Test fun undoRestoresTheOriginalPosition() = runBlocking {
        var clock = 100L
        val repo = DataStoreSavedSpotsRepository(store().first) { clock++ }
        repo.save("a"); repo.save("b"); repo.save("c")
        val removed = repo.saved.first().first { it.spotId == "b" }
        repo.remove("b")
        assertEquals(listOf("c", "a"), repo.saved.first().map { it.spotId })
        repo.restore(removed.spotId, removed.savedAt)
        assertEquals(listOf("c", "b", "a"), repo.saved.first().map { it.spotId })
    }

    @Test fun toggleAndIsSaved() = runBlocking {
        val repo = DataStoreSavedSpotsRepository(store().first)
        repo.toggle("a"); assertTrue(repo.isSaved("a").first())
        repo.toggle("a"); assertFalse(repo.isSaved("a").first())
    }

    @Test fun legacyIdSetIsMigratedWithoutLosingAnything() = runBlocking {
        // Write the Step 5 format (a plain set of IDs) with no migration, then close that store.
        val (legacy, legacyScope) = store(migrate = false)
        legacy.edit { it[LegacySavedIdsMigration.LegacyIdsKey] = setOf("cafe-dulce", "pizza-house", "mang-juans-carinderia") }
        legacyScope.cancel()

        val repo = DataStoreSavedSpotsRepository(store().first)
        val ids = repo.saved.first().map { it.spotId }
        assertEquals(setOf("cafe-dulce", "pizza-house", "mang-juans-carinderia"), ids.toSet())
        repo.save("ricos-pizza") // saved later than any migrated spot: on top
        assertEquals("ricos-pizza", repo.saved.first().first().spotId)
    }

    @Test fun malformedEntriesAreSkipped() {
        val decoded = SavedEntryCodec.decode(setOf("123:good", "nope", ":empty-time", "456:", "789:has:colons"))
        assertEquals(listOf("good" to 123L, "has:colons" to 789L), decoded.map { it.spotId to it.savedAt }.sortedBy { it.second })
    }
}
