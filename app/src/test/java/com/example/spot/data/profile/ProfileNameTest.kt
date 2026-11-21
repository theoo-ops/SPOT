package com.example.spot.data.profile

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ProfileNameTest {
    @get:Rule val folder = TemporaryFolder()

    @Test fun handleFromFirstWord() {
        assertEquals("@maryleinspot", ProfileName.handle("Marylein Orquinaza"))
        assertEquals("@josespot", ProfileName.handle("  José   Rizal "))
        assertEquals("@jo2spot", ProfileName.handle("Jo-2 Cruz"))
        assertEquals("@guestspot", ProfileName.handle(""))
    }

    @Test fun handleFallsBackToSpotter() {
        assertEquals("@spotter", ProfileName.handle("🍜🍜 Ramen"))
        assertEquals("@spotter", ProfileName.handle("مريم الأحمد"))
    }

    @Test fun normalizeTrimsLimitsAndDefaults() {
        assertEquals("Guest", ProfileName.normalize("   "))
        assertEquals("Ana", ProfileName.normalize("  Ana  "))
        assertEquals(30, ProfileName.length(ProfileName.normalize("A".repeat(45))))
    }

    @Test fun limitNeverSplitsAnEmoji() {
        val name = "A".repeat(29) + "👩‍🍳" + "B" // the 30th character is a multi-code-point emoji
        val cut = ProfileName.normalize(name)
        assertEquals(30, ProfileName.length(cut))
        assertEquals("A".repeat(29) + "👩‍🍳", cut)
    }

    @Test fun initials() {
        assertEquals("G", ProfileName.initials("Guest"))
        assertEquals("MO", ProfileName.initials("Marylein Orquinaza"))
        assertEquals("🍜R", ProfileName.initials("🍜 ramen"))
    }

    @Test fun repositoryDefaultsToGuestAndStoresNormalised() = runBlocking {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val repo = DataStoreProfileRepository(PreferenceDataStoreFactory.create(scope = scope) { folder.root.resolve("p.preferences_pb") })
        assertEquals("Guest", repo.displayName.first())
        repo.setDisplayName("   Marylein Orquinaza   ")
        assertEquals("Marylein Orquinaza", repo.displayName.first())
        repo.setDisplayName("")
        assertEquals("Guest", repo.displayName.first())
        scope.cancel()
    }
}
