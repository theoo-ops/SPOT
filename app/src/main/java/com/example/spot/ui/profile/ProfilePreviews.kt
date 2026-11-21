package com.example.spot.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.spot.R
import com.example.spot.data.profile.ProfileName
import com.example.spot.ui.theme.SpotTheme

// break-ui fixtures: they enter through ProfileUiState / screen parameters, the same boundary the app uses.

private fun state(name: String, favorites: Int) = ProfileUiState(
    name = ProfileName.normalize(name),
    handle = ProfileName.handle(name),
    initials = ProfileName.initials(name),
    isGuest = ProfileName.normalize(name) == ProfileName.Guest,
    stats = listOf(ProfileStat(favorites, R.plurals.profile_stat_favorites, R.plurals.profile_stat_favorites_spoken)),
)

@Composable
private fun Themed(content: @Composable () -> Unit) = SpotTheme {
    Box(Modifier.background(MaterialTheme.colorScheme.background)) { content() }
}

@Composable
private fun PreviewProfile(state: ProfileUiState) = Themed { ProfileScreen(state, {}, {}, {}, {}, {}) }

@Preview(name = "1 Mockup: Marylein, 3 favorites", widthDp = 360, heightDp = 760)
@Composable
private fun ProfileMockupPreview() = PreviewProfile(state("Marylein Orquinaza", 3))

@Preview(name = "2 Guest default, 0 favorites", widthDp = 360, heightDp = 760)
@Composable
private fun ProfileGuestPreview() = PreviewProfile(state("", 0))

@Preview(name = "3 30-character name with emoji, 500 favorites", widthDp = 360, heightDp = 760)
@Composable
private fun ProfileLongNamePreview() = PreviewProfile(state("Marylein Orquinaza dela Cruz 🍜", 500))

@Preview(name = "4 Right-to-left name (handle falls back to @spotter)", widthDp = 360, heightDp = 760)
@Composable
private fun ProfileRtlPreview() = PreviewProfile(state("مريم الأحمد", 12))

@Preview(name = "5 Font 200%", widthDp = 360, heightDp = 1500, fontScale = 2f)
@Composable
private fun ProfileLargeFontPreview() = PreviewProfile(state("Marylein Orquinaza dela Cruz 🍜", 500))

@Preview(name = "6 Small screen", widthDp = 320, heightDp = 568)
@Composable
private fun ProfileSmallPreview() = PreviewProfile(state("Marylein Orquinaza", 3))

@Preview(name = "7 Edit sheet content, 30/30", widthDp = 360)
@Composable
private fun EditSheetPreview() = Themed { EditNameContent("Marylein Orquinaza dela Cruz 🍜", {}, {}, {}) }

@Preview(name = "8 App Settings: precise", widthDp = 360, heightDp = 640)
@Composable
private fun SettingsPrecisePreview() = Themed { AppSettingsScreen(LocationAccess.Precise, "0.1.0", {}, {}) }

@Preview(name = "9 App Settings: approximate", widthDp = 360, heightDp = 640)
@Composable
private fun SettingsApproximatePreview() = Themed { AppSettingsScreen(LocationAccess.Approximate, "0.1.0", {}, {}) }

@Preview(name = "10 App Settings: off (denied or don't ask again), font 200%", widthDp = 360, heightDp = 900, fontScale = 2f)
@Composable
private fun SettingsOffPreview() = Themed { AppSettingsScreen(LocationAccess.Off, "0.1.0", {}, {}) }

@Preview(name = "11 About", widthDp = 360, heightDp = 760)
@Composable
private fun AboutPreview() = Themed { AboutScreen("0.1.0", {}) }

@Preview(name = "12 Help", widthDp = 360, heightDp = 900)
@Composable
private fun HelpPreview() = Themed { HelpScreen(remember { SnackbarHostState() }, {}, {}) }
