package com.example.spot.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.spot.R
import com.example.spot.ui.components.RatingLabel
import com.example.spot.ui.motion.PressMotion
import com.example.spot.ui.motion.rememberPressScale

private val CardShape = RoundedCornerShape(16.dp)

// ---------- Header ----------

@Composable
internal fun HomeHeader(greeting: Greeting, onProfileClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f).padding(top = 4.dp)) {
            Text(
                text = stringResource(greeting.label),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        AvatarButton(onProfileClick)
    }
}

private val Greeting.label: Int
    get() = when (this) {
        Greeting.Morning -> R.string.greeting_morning
        Greeting.Afternoon -> R.string.greeting_afternoon
        Greeting.Evening -> R.string.greeting_evening
    }

@Composable
private fun AvatarButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction)
    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(interaction, ripple(), role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = stringResource(R.string.cd_profile),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

// ---------- Search ----------

/** Looks like a field, behaves like a button: it opens the Search screen. */
@Composable
internal fun SearchField(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction, PressMotion.CardScale)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CardShape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                role = Role.Button,
                onClickLabel = stringResource(R.string.home_search_open),
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.home_search_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ---------- Map preview ----------

@Composable
internal fun MapPreviewCard(onViewMapClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(168.dp)
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
    ) {
        MapPlaceholder(Modifier.matchParentSize())
        PillButton(
            text = stringResource(R.string.home_view_map),
            iconRes = R.drawable.ic_map,
            onClick = onViewMapClick,
            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
        )
    }
}

/** Static stand-in until the real map arrives with the Explore step. Decorative only. */
@Composable
private fun MapPlaceholder(modifier: Modifier = Modifier) {
    val pin = MaterialTheme.colorScheme.primary
    Box(modifier) {
        Canvas(Modifier.matchParentSize()) {
            drawRect(Color(0xFFEAF1EA))
            val step = 28.dp.toPx()
            var x = step
            while (x < size.width) { drawLine(Color.White.copy(alpha = 0.7f), Offset(x, 0f), Offset(x, size.height), 1.dp.toPx()); x += step }
            var y = step
            while (y < size.height) { drawLine(Color.White.copy(alpha = 0.7f), Offset(0f, y), Offset(size.width, y), 1.dp.toPx()); y += step }
            drawOval(Color(0xFFD6E6F7), Offset(size.width * 0.70f, size.height * 0.12f), Size(size.width * 0.28f, size.height * 0.5f))
            drawRoundRect(Color(0xFFD3E8D2), Offset(size.width * 0.16f, size.height * 0.62f), Size(size.width * 0.26f, size.height * 0.28f), CornerRadius(8.dp.toPx()))
            drawLine(Color.White, Offset(0f, size.height * 0.62f), Offset(size.width, size.height * 0.30f), 14.dp.toPx(), StrokeCap.Butt)
            drawLine(Color.White, Offset(size.width * 0.58f, 0f), Offset(size.width * 0.50f, size.height), 10.dp.toPx(), StrokeCap.Butt)
            val dot = Offset(size.width * 0.29f, size.height * 0.76f)
            drawCircle(Color(0x332F80ED), 14.dp.toPx(), dot)
            drawCircle(Color.White, 7.dp.toPx(), dot)
            drawCircle(Color(0xFF2F80ED), 5.dp.toPx(), dot)
        }
        Icon(Icons.Filled.LocationOn, null, tint = pin, modifier = Modifier.align(BiasAlignment(-0.15f, -0.45f)).size(36.dp))
        Icon(Icons.Filled.LocationOn, null, tint = pin, modifier = Modifier.align(BiasAlignment(0.45f, -0.05f)).size(28.dp))
    }
}

/** White pill used for "View Map" and "Show all spots". */
@Composable
internal fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction)
    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .shadow(2.dp, CircleShape)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .clickable(interaction, ripple(), role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconRes != null) {
            Icon(painterResource(iconRes), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ---------- Section header ----------

@Composable
internal fun SectionHeader(onSeeAllClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.home_nearby_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        val interaction = remember { MutableInteractionSource() }
        val scale = rememberPressScale(interaction)
        Row(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
                .clip(CircleShape)
                .clickable(interaction, ripple(), role = Role.Button, onClick = onSeeAllClick)
                .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.home_see_all), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
    }
}

// ---------- Spot card ----------

@Composable
internal fun SpotCard(spot: SpotItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction, PressMotion.CardScale)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .shadow(2.dp, CardShape)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .clickable(interaction, ripple(), role = Role.Button, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpotPhoto(spot.photo, Modifier.size(64.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = spot.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                // Bullet stays with the category; no distance when location is unknown.
                text = spot.distanceLabel?.let { "${spot.category}\u00A0• $it" } ?: spot.category,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            RatingLabel(spot.rating)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Photo, or a tinted placeholder with the fork-and-knife icon when there is none. */
@Composable
private fun SpotPhoto(@DrawableRes photo: Int?, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    if (photo != null) {
        Image(painterResource(photo), contentDescription = null, contentScale = ContentScale.Crop, modifier = modifier.clip(shape))
    } else {
        Box(modifier.clip(shape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.ic_restaurant),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

/** Static (no shimmer: Home is seen constantly and a loop would be noise). */
@Composable
internal fun SpotCardSkeleton(modifier: Modifier = Modifier) {
    val block = MaterialTheme.colorScheme.surfaceContainerHigh
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(block))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth(0.7f).height(16.dp).clip(CircleShape).background(block))
            Box(Modifier.fillMaxWidth(0.45f).height(12.dp).clip(CircleShape).background(block))
            Box(Modifier.width(36.dp).height(12.dp).clip(CircleShape).background(block))
        }
    }
}

// ---------- Empty ----------

@Composable
internal fun EmptyCategory(category: String?, onShowAll: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_restaurant), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (category != null) stringResource(R.string.home_empty_title, category) else stringResource(R.string.home_empty_title_all),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(if (category != null) R.string.home_empty_body else R.string.home_empty_body_all),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (category != null) {
            Spacer(Modifier.height(12.dp))
            PillButton(stringResource(R.string.home_empty_action), onClick = onShowAll)
        }
    }
}
