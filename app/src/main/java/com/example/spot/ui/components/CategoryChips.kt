package com.example.spot.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.spot.R
import com.example.spot.ui.motion.quickColorSpec
import com.example.spot.ui.motion.rememberPressScale

/** Category filter chips, shared by Home and Explore Map. "All" = null. */
@Composable
fun CategoryChips(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val all = stringResource(R.string.home_chip_all)
    LazyRow(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "all") { CategoryChip(all, selected == null) { onSelect(null) } }
        items(categories, key = { it }) { category ->
            CategoryChip(category, selected == category) { onSelect(category) }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction)
    val container = animateColorAsState(
        if (selected) colors.primary else colors.surfaceContainerLowest, quickColorSpec(), label = "chipContainer",
    )
    val border = animateColorAsState(
        if (selected) colors.primary else colors.outlineVariant, quickColorSpec(), label = "chipBorder",
    )
    val content by animateColorAsState(
        if (selected) colors.onPrimary else colors.onSurface, quickColorSpec(), label = "chipContent",
    )
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize() // 48dp touch target around the 36dp pill
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .clip(CircleShape)
            .drawBehind {
                drawRect(container.value)
                drawRoundRect(
                    color = border.value,
                    cornerRadius = CornerRadius(size.height / 2),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            .selectable(
                selected = selected,
                interactionSource = interaction,
                indication = ripple(),
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = content, maxLines = 1)
    }
}
