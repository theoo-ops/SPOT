package com.example.spot.ui.explore

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.spot.ui.home.PillButton
import com.example.spot.ui.motion.LocalAnimationsEnabled
import com.example.spot.ui.motion.PressMotion
import com.example.spot.ui.motion.rememberPressScale
import com.example.spot.ui.theme.Burgundy
import com.example.spot.ui.theme.BurgundyDeep

/** Pin heights, shared with the callout so it sits just above the selected pin. */
internal val PinSize = 36.dp
internal val SelectedPinSize = 48.dp
private val UserBlue = Color(0xFF2F80ED)

// ---------- Top bar ----------

@Composable
internal fun ExploreTopBar(onBack: () -> Unit, onFilter: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 4.dp)) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
        }
        Text(
            text = stringResource(R.string.explore_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 56.dp).semantics { heading() },
        )
        IconButton(onClick = onFilter, modifier = Modifier.align(Alignment.CenterEnd)) {
            Icon(painterResource(R.drawable.ic_tune), contentDescription = stringResource(R.string.cd_filters))
        }
    }
}

// ---------- Markers (rendered to bitmaps by MarkerComposable) ----------

@Composable
internal fun SpotPin(selected: Boolean) {
    Icon(
        imageVector = Icons.Filled.LocationOn,
        contentDescription = null,
        tint = if (selected) BurgundyDeep else Burgundy,
        modifier = Modifier.size(if (selected) SelectedPinSize else PinSize),
    )
}

@Composable
internal fun UserDot() {
    Box(
        Modifier.size(20.dp).shadow(2.dp, CircleShape).clip(CircleShape).background(Color.White).padding(3.dp)
            .clip(CircleShape).background(UserBlue),
    )
}

internal val UserHaloFill = UserBlue.copy(alpha = 0.15f)
internal val UserHaloStroke = UserBlue.copy(alpha = 0.4f)

// ---------- Callout ----------

/**
 * Card above the selected pin. Appears from the pin: quick ease-out fade + scale from its bottom
 * centre (snaps when animations are off). Disappears instantly.
 */
@Composable
internal fun SpotCallout(spot: MapSpot, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val animate = LocalAnimationsEnabled.current
    val appear = remember(spot.id) { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(spot.id) {
        if (animate) appear.animateTo(1f, tween(ExploreMotion.CalloutMs, easing = PressMotion.EaseOut))
    }
    val interaction = remember { MutableInteractionSource() }
    val press = rememberPressScale(interaction, PressMotion.CardScale)
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .graphicsLayer {
                val s = (ExploreMotion.CalloutStartScale + (1 - ExploreMotion.CalloutStartScale) * appear.value) * press.value
                scaleX = s
                scaleY = s
                alpha = appear.value
                transformOrigin = TransformOrigin(0.5f, 1f) // grows out of the pin
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier
                .widthIn(min = 140.dp, max = 260.dp)
                .shadow(6.dp, shape)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .clickable(
                    interactionSource = interaction,
                    indication = ripple(),
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.explore_callout_open),
                    onClick = onClick,
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = spot.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RatingLabel(spot.rating, style = MaterialTheme.typography.labelMedium)
                spot.distanceLabel?.let {
                    Text(
                        text = "  •  $it",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
        // Pointer
        val pointerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        Canvas(Modifier.size(width = 16.dp, height = 8.dp)) {
            drawPath(
                Path().apply {
                    moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(size.width / 2, size.height); close()
                },
                pointerColor,
            )
        }
    }
}

// ---------- Banner ----------

enum class BannerKind(@param:StringRes val message: Int, @param:StringRes val action: Int?) {
    PermissionDenied(R.string.explore_banner_denied, R.string.explore_banner_turn_on_location),
    PermissionDeniedForever(R.string.explore_banner_denied, R.string.explore_banner_open_settings),
    LocationServicesOff(R.string.explore_banner_services_off, R.string.explore_banner_turn_on),
    Approximate(R.string.explore_banner_approximate, R.string.explore_banner_use_precise),
    Offline(R.string.explore_banner_offline, null),
}

@Composable
internal fun LocationBanner(kind: BannerKind, onAction: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(3.dp, shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(start = 14.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(kind.message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f).padding(vertical = 8.dp),
        )
        kind.action?.let { action ->
            Text(
                text = stringResource(action),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClick = onAction)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_dismiss), modifier = Modifier.size(20.dp))
        }
    }
}

// ---------- Locate button ----------

@Composable
internal fun LocateButton(isLocating: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction)
    Box(
        modifier = modifier
            .size(56.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .clickable(interaction, ripple(), role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isLocating) {
            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
        }
        Icon(
            painter = painterResource(R.drawable.ic_my_location),
            contentDescription = stringResource(if (isLocating) R.string.cd_locating else R.string.cd_locate),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(if (isLocating) 12.dp else 24.dp),
        )
    }
}

// ---------- Empty category ----------

@Composable
internal fun EmptyMapMessage(category: String, onShowAll: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .widthIn(max = 320.dp)
            .shadow(4.dp, shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.explore_empty_title, category),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.explore_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.width(1.dp).height(4.dp))
        PillButton(stringResource(R.string.home_empty_action), onClick = onShowAll)
    }
}

object ExploreMotion {
    const val CalloutMs = 140
    const val CalloutStartScale = 0.9f
    const val CameraZoom = 16f
}
