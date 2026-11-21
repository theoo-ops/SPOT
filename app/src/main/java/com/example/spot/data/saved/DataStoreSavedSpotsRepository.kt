package com.example.spot.data.saved

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/** One DataStore file for saved spots. Created once per process (DataStore requires a single instance). */
val Context.savedSpotsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "saved_spots",
    produceMigrations = { listOf(LegacySavedIdsMigration) },
)

/**
 * Saved spots on Preferences DataStore. Each entry is stored as "savedAt:spotId" in a string set
 * (Preferences has no map type), so the time travels with the ID.
 */
class DataStoreSavedSpotsRepository(
    private val store: DataStore<Preferences>,
    private val now: () -> Long = System::currentTimeMillis,
) : SavedSpotsRepository {

    override val saved: Flow<List<SavedEntry>> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e } // unreadable file: show nothing, don't crash
        .map { prefs -> SavedEntryCodec.decode(prefs[EntriesKey]).newestFirst() }
        .distinctUntilChanged()

    override suspend fun save(spotId: String) = update { if (spotId in it) it else it + (spotId to now()) }
    override suspend fun remove(spotId: String) = update { it - spotId }
    override suspend fun restore(spotId: String, savedAt: Long) = update { it + (spotId to savedAt) }
    override suspend fun isSavedNow(spotId: String) = saved.first().any { it.spotId == spotId }

    private suspend fun update(change: (Map<String, Long>) -> Map<String, Long>) {
        store.edit { prefs ->
            val current = SavedEntryCodec.decode(prefs[EntriesKey]).associate { it.spotId to it.savedAt }
            prefs[EntriesKey] = SavedEntryCodec.encode(change(current))
        }
    }

    internal companion object {
        val EntriesKey = stringSetPreferencesKey("saved_spot_entries")
    }
}

internal object SavedEntryCodec {
    fun encode(entries: Map<String, Long>): Set<String> = entries.map { (id, at) -> "$at:$id" }.toSet()

    /** Skips anything malformed instead of crashing. */
    fun decode(raw: Set<String>?): List<SavedEntry> = raw.orEmpty().mapNotNull { item ->
        val colon = item.indexOf(':')
        val at = item.substring(0, colon.coerceAtLeast(0)).toLongOrNull()
        val id = if (colon >= 0) item.substring(colon + 1) else ""
        if (at == null || id.isEmpty()) null else SavedEntry(id, at)
    }
}

/**
 * Step 5 stored saved spots as a plain set of IDs. This moves them into timed entries once,
 * keeps every ID, and removes the old key. Old IDs have no saved time, so they get "now"
 * (stepped by 1 ms so their order stays stable) and appear below anything saved later.
 * If your Step 5 used a different key name, change [LegacyIdsKey].
 */
internal object LegacySavedIdsMigration : DataMigration<Preferences> {
    val LegacyIdsKey = stringSetPreferencesKey("saved_spot_ids")

    override suspend fun shouldMigrate(currentData: Preferences) = LegacyIdsKey in currentData

    override suspend fun migrate(currentData: Preferences): Preferences {
        val prefs = currentData.toMutablePreferences()
        val entries = SavedEntryCodec.decode(prefs[DataStoreSavedSpotsRepository.EntriesKey])
            .associate { it.spotId to it.savedAt }.toMutableMap()
        val base = System.currentTimeMillis()
        prefs[LegacyIdsKey].orEmpty().sorted().forEachIndexed { index, id ->
            if (id !in entries) entries[id] = base - index
        }
        prefs[DataStoreSavedSpotsRepository.EntriesKey] = SavedEntryCodec.encode(entries)
        prefs.remove(LegacyIdsKey)
        return prefs.toPreferences()
    }

    override suspend fun cleanUp() = Unit
}
