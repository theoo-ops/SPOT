package com.example.spot.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.spot.R
import com.example.spot.ui.home.PillButton
import com.example.spot.ui.motion.LocalAnimationsEnabled
import com.example.spot.ui.motion.PressMotion
import kotlinx.coroutines.delay

/** Rounded search field: leading icon, placeholder, and a clear button that fades in only with text. */
@Composable
internal fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(24.dp)
    val label = stringResource(R.string.search_field_label)
    // The ViewModel owns the text; this keeps the cursor (saved across rotation and tab switches).
    // When the text changes from outside (clear button), the cursor goes to the end.
    var lastValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(query, TextRange(query.length)))
    }
    val value = if (lastValue.text == query) lastValue else TextFieldValue(query, TextRange(query.length))
    BasicTextField(
        value = value,
        onValueChange = {
            lastValue = it
            if (it.text != query) onQueryChange(it.text)
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .semantics { contentDescription = label },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
        cursorBrush = SolidColor(colors.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(shape)
                    .background(colors.surfaceContainerLowest)
                    .border(1.dp, colors.outlineVariant, shape)
                    .padding(start = 14.dp, end = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f).padding(vertical = 12.dp)) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_field_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerTextField()
                }
                val animate = LocalAnimationsEnabled.current
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = if (animate) fadeIn(tween(SearchMotion.ClearFadeMs, easing = PressMotion.EaseOut)) else EnterTransition.None,
                    exit = if (animate) fadeOut(tween(SearchMotion.ClearFadeMs, easing = PressMotion.EaseOut)) else ExitTransition.None,
                ) {
                    IconButton(onClick = onClear) {
                        Box(
                            Modifier.size(20.dp).clip(CircleShape).background(colors.onSurfaceVariant.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.cd_clear_search),
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }
        },
    )
}

/** No results for a query, or an empty category. Plain, active wording. */
@Composable
internal fun NoResults(
    query: String,
    category: String?,
    onClearQuery: () -> Unit,
    onShowAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val trimmed = query.trim()
    Column(modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_restaurant), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = when {
                trimmed.isNotEmpty() -> stringResource(R.string.search_empty_title, trimmed)
                category != null -> stringResource(R.string.home_empty_title, category)
                else -> stringResource(R.string.home_empty_title_all)
            },
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 3, // a very long query stays readable
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(if (trimmed.isNotEmpty()) R.string.search_empty_body else R.string.home_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        when {
            trimmed.isNotEmpty() -> PillButton(stringResource(R.string.search_empty_action), onClick = onClearQuery)
            category != null -> PillButton(stringResource(R.string.home_empty_action), onClick = onShowAll)
        }
    }
}

/**
 * Tells screen readers how many results there are ("3 results"). Waits until typing settles,
 * so TalkBack doesn't read a count after every keystroke.
 */
@Composable
internal fun ResultCountAnnouncer(isLoading: Boolean, count: Int) {
    val text = when {
        isLoading -> ""
        count == 0 -> stringResource(R.string.search_no_results)
        else -> pluralStringResource(R.plurals.search_results_count, count, count)
    }
    var announced by remember { mutableStateOf("") }
    LaunchedEffect(text) {
        delay(SearchMotion.AnnounceDelayMs)
        announced = text
    }
    Box(
        Modifier.size(1.dp).semantics {
            liveRegion = LiveRegionMode.Polite
            contentDescription = announced
        },
    )
}

object SearchMotion {
    const val ClearFadeMs = 100
    const val AnnounceDelayMs = 700L
}
