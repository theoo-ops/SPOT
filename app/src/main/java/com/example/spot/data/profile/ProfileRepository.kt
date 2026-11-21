package com.example.spot.data.profile

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.text.BreakIterator
import java.text.Normalizer
import java.util.Locale

/** The local profile: just a display name. No sign-in, no network; nothing leaves the device. */
interface ProfileRepository {
    /** Always normalised (see [ProfileName.normalize]); "Guest" until the user sets a name. */
    val displayName: Flow<String>

    suspend fun setDisplayName(name: String)
}

/** The one place the name rules live. "Characters" are what the user sees (an emoji counts as one). */
object ProfileName {
    const val Guest = "Guest"
    const val MaxLength = 30
    private const val Fallback = "spotter"

    /** Trimmed, at most [MaxLength] characters, and [Guest] when empty. */
    fun normalize(raw: String): String = take(raw.trim(), MaxLength).trim().ifEmpty { Guest }

    /**
     * "@" + the first word, lower-cased, accents removed, letters and digits only, + "spot".
     * "Marylein Orquinaza" -> "@maryleinspot". Nothing usable left (emoji, Arabic...) -> "@spotter".
     */
    fun handle(name: String): String {
        val firstWord = normalize(name).split(Regex("\\s+")).first()
        val ascii = Normalizer.normalize(firstWord.lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .filter { it in 'a'..'z' || it in '0'..'9' }
        return "@" + if (ascii.isEmpty()) Fallback else ascii + "spot"
    }

    /** First character of the first two words, upper-cased: "Marylein Orquinaza" -> "MO", "Guest" -> "G". */
    fun initials(name: String): String =
        normalize(name).split(Regex("\\s+")).take(2).joinToString("") { take(it, 1).uppercase() }

    /** Number of user-visible characters. */
    fun length(text: String): Int = graphemeBoundaries(text).size - 1

    /** The first [count] user-visible characters, never splitting an emoji or accent. */
    fun take(text: String, count: Int): String {
        val bounds = graphemeBoundaries(text)
        return if (bounds.size - 1 <= count) text else text.substring(0, bounds[count])
    }

    private fun graphemeBoundaries(text: String): List<Int> {
        val it = BreakIterator.getCharacterInstance().apply { setText(text) }
        val bounds = mutableListOf(it.first())
        var next = it.next()
        while (next != BreakIterator.DONE) { bounds += next; next = it.next() }
        return bounds
    }
}

val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = "profile")

class DataStoreProfileRepository(private val store: DataStore<Preferences>) : ProfileRepository {
    override val displayName: Flow<String> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { ProfileName.normalize(it[NameKey].orEmpty()) }
        .distinctUntilChanged()

    override suspend fun setDisplayName(name: String) {
        store.edit { it[NameKey] = ProfileName.normalize(name) }
    }

    private companion object {
        val NameKey = stringPreferencesKey("display_name")
    }
}

/** Previews, tests and the debug data set. */
class InMemoryProfileRepository(initial: String = ProfileName.Guest) : ProfileRepository {
    private val name = MutableStateFlow(ProfileName.normalize(initial))
    override val displayName: Flow<String> = name
    override suspend fun setDisplayName(name: String) {
        this.name.value = ProfileName.normalize(name)
    }
}
