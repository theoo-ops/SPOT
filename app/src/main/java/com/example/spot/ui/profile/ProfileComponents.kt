package com.example.spot.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.spot.R
import com.example.spot.data.profile.ProfileName
import com.example.spot.ui.motion.PressMotion
import com.example.spot.ui.motion.rememberPressScale
import kotlinx.coroutines.launch

private val CardShape = RoundedCornerShape(16.dp)

/** Initials on blush. One touch target that reads "Edit profile". */
@Composable
internal fun ProfileAvatar(initials: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction)
    val label = stringResource(R.string.profile_edit)
    Box(
        modifier = modifier
            .size(96.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .border(4.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape)
            .padding(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(interaction, ripple(), role = Role.Button, onClick = onClick)
            .clearAndSetSemantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
        )
    }
}

/** Name (2 lines max) and the derived handle; tapping opens the edit sheet too. */
@Composable
internal fun ProfileNameBlock(name: String, handle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.profile_edit), onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = handle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Built from a list, so more stats slot in later. Each cell reads as one phrase ("3 favorites"). */
@Composable
internal fun StatsCard(stats: List<ProfileStat>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .shadow(2.dp, CardShape)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        stats.forEachIndexed { index, stat ->
            if (index > 0) VerticalDivider(Modifier.fillMaxHeight(0.6f), color = MaterialTheme.colorScheme.outlineVariant)
            val spoken = pluralStringResource(stat.spoken, stat.value, stat.value)
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp).clearAndSetSemantics { contentDescription = spoken },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("${stat.value}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text(
                    text = pluralStringResource(stat.label, stat.value),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center, // wraps instead of clipping at 200% font
                )
            }
        }
    }
}

/** Icon, label, chevron: one 56dp touch target with merged semantics. */
@Composable
internal fun ProfileRow(icon: Painter, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction, PressMotion.CardScale)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .shadow(1.dp, CardShape)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .clickable(interaction, ripple(), role = Role.Button, onClick = onClick) // merges the row into one node
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Default Material sheet (its own restrained motion). Empty = Guest. Counter shows characters as seen. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditNameSheet(currentName: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var text by rememberSaveable { mutableStateOf(currentName) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    fun close(then: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { then() }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        EditNameContent(
            text = text,
            onTextChange = { text = ProfileName.take(it, ProfileName.MaxLength) }, // never more than 30 characters
            onSave = { close { onSave(text) } },
            onCancel = { close(onDismiss) },
            focusRequester = focus,
        )
    }
}

@Composable
internal fun EditNameContent(
    text: String,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp).imePadding()) {
        Text(
            text = stringResource(R.string.profile_sheet_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.profile_sheet_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            label = { Text(stringResource(R.string.profile_sheet_label)) },
            placeholder = { Text(ProfileName.Guest) },
            singleLine = true,
            supportingText = {
                Text(
                    text = stringResource(R.string.profile_sheet_counter, ProfileName.length(text), ProfileName.MaxLength),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSave() }),
        )
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.profile_sheet_cancel)) }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onSave) { Text(stringResource(R.string.profile_sheet_save)) }
        }
    }
}
