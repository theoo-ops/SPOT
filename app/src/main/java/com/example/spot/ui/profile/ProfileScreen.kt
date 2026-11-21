package com.example.spot.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spot.R

@Composable
fun ProfileRoute(
    onOpenSaved: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenHelp: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreen(state, viewModel::saveName, onOpenSaved, onOpenSettings, onOpenAbout, onOpenHelp)
}

/** Tab root: no back arrow. The whole screen scrolls (small phones, 200% font). */
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onSaveName: (String) -> Unit,
    onOpenSaved: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenHelp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.profile_settings))
            }
        }
        Spacer(Modifier.height(16.dp))

        ProfileAvatar(initials = state.initials, onClick = { editing = true })
        Spacer(Modifier.height(12.dp))
        ProfileNameBlock(name = state.name, handle = state.handle, onClick = { editing = true })
        Spacer(Modifier.height(20.dp))

        StatsCard(state.stats)
        Spacer(Modifier.height(20.dp))

        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ProfileRow(painterResource(R.drawable.ic_bookmark_border), stringResource(R.string.profile_row_saved), onOpenSaved)
            ProfileRow(rememberVectorPainter(Icons.Outlined.Settings), stringResource(R.string.profile_row_settings), onOpenSettings)
            ProfileRow(rememberVectorPainter(Icons.Outlined.Info), stringResource(R.string.profile_row_about), onOpenAbout)
            ProfileRow(painterResource(R.drawable.ic_help_outline), stringResource(R.string.profile_row_help), onOpenHelp)
        }
    }

    if (editing) {
        EditNameSheet(
            currentName = if (state.isGuest) "" else state.name,
            onSave = { onSaveName(it); editing = false },
            onDismiss = { editing = false },
        )
    }
}
