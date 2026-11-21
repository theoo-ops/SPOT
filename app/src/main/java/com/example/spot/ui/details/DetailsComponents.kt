package com.example.spot.ui.details

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import com.example.spot.R
import com.example.spot.location.Distance
import com.example.spot.ui.motion.LocalAnimationsEnabled
import com.example.spot.ui.motion.rememberPressScale
import com.example.spot.ui.theme.StarYellow
import kotlinx.coroutines.launch
import java.util.Locale

/** Same side margin for the details and the pinned buttons. */
internal val ContentMargin = 24.dp

// ---------- Hero ----------

/** Photo cropped to fill, edge to edge behind the status bar, straight bottom edge. The scrim is pinned (see [TopScrim]). */
@Composable
internal fun DetailsHero(@DrawableRes photo: Int?, height: Dp, statusTop: Dp, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(height)) {
        if (photo != null) {
            Image(
                painter = painterResource(photo),
                contentDescription = null, // decorative here; the viewer describes it
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            // Missing photo: the same blush placeholder as the list thumbnails, centered below the status bar.
            Box(
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer).padding(top = statusTop),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_restaurant),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(72.dp),
                )
            }
        }
    }
}

/**
 * Subtle dark fade behind the status bar and the two circle buttons, pinned so it stays under the
 * white status bar icons while the photo scrolls. Exactly the top bar's height: never over the text.
 */
@Composable
internal fun TopScrim(height: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.40f), Color.Transparent))),
    )
}

// ---------- Body ----------

@Composable
internal fun DetailsBody(spot: SpotDetails, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(horizontal = ContentMargin).padding(top = 24.dp, bottom = 24.dp)) {
        Text(
            text = spot.name,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, fontSize = 26.sp, lineHeight = 32.sp),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(8.dp))
        MetaRow(spot.rating, spot.reviewCount, spot.distanceMeters)
        spot.category?.let {
            Spacer(Modifier.height(16.dp))
            CategoryChip(it)
        }
        if (spot.hours != null || spot.address != null) Spacer(Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            spot.hours?.let { InfoRow(painterResource(R.drawable.ic_schedule), stringResource(R.string.details_hours), it) }
            spot.address?.let { InfoRow(painterResource(R.drawable.ic_explore), stringResource(R.string.details_location), it) }
        }
    }
}

/** ★ 4.6 (120 reviews) · 📍 200 m. Wraps at large font sizes; read as one phrase. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MetaRow(rating: Double?, reviewCount: Int?, distanceMeters: Double?) {
    val ratingText = rating?.let { String.format(Locale.US, "%.1f", it) }
    val spoken = listOfNotNull(
        ratingText?.let { stringResource(R.string.details_cd_rating, it) } ?: stringResource(R.string.details_cd_unrated),
        reviewCount?.let { pluralStringResource(R.plurals.details_reviews_spoken, it, it) },
        distanceMeters?.let { Distance.format(it) },
    ).joinToString(", ")
    FlowRow(
        modifier = Modifier.clearAndSetSemantics { contentDescription = spoken },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (ratingText != null) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = StarYellow, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(ratingText, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                reviewCount?.let {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        pluralStringResource(R.plurals.details_reviews, it, it),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Text(stringResource(R.string.rating_new), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
            }
        }
        distanceMeters?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_place), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(Distance.formatShort(it), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/** Not tappable: plain text on a pill. */
@Composable
private fun CategoryChip(category: String) {
    Text(
        text = category,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

/** Icon column, label, value below. Values wrap in full (an address must be readable). */
@Composable
private fun InfoRow(icon: Painter, label: String, value: String) {
    Row(Modifier.fillMaxWidth().semantics(mergeDescendants = true) {}) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 1.dp).size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ---------- Pinned actions ----------

@Composable
internal fun DetailsActions(
    hasPhoto: Boolean,
    showTopDivider: Boolean,
    onGetDirections: () -> Unit,
    onViewPhotos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
        // Always 1dp tall so the buttons never shift; only visible while content continues underneath.
        HorizontalDivider(color = if (showTopDivider) MaterialTheme.colorScheme.outlineVariant else Color.Transparent)
        Column(
            modifier = modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = ContentMargin)
                .padding(top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val directions = remember { MutableInteractionSource() }
            val directionsScale = rememberPressScale(directions)
            Button(
                onClick = onGetDirections,
                shape = CircleShape,
                interactionSource = directions,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    .graphicsLayer { scaleX = directionsScale.value; scaleY = directionsScale.value },
            ) {
                Icon(painterResource(R.drawable.ic_navigation), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.details_directions), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
            }
            if (hasPhoto) { // no photo: nothing to view, so no button
                val photos = remember { MutableInteractionSource() }
                val photosScale = rememberPressScale(photos)
                OutlinedButton(
                    onClick = onViewPhotos,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    interactionSource = photos,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                        .graphicsLayer { scaleX = photosScale.value; scaleY = photosScale.value },
                ) {
                    Icon(painterResource(R.drawable.ic_photo_camera), contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.details_view_photos), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                }
            }
        }
    }
}

// ---------- Circle buttons ----------

/** 48dp touch area around a 40dp white circle; the icon's description names the button. */
@Composable
private fun CircleButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction)
    Box(
        modifier = modifier
            .size(48.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .clickable(interaction, ripple(bounded = false, radius = 24.dp), role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(40.dp).shadow(3.dp, CircleShape).clip(CircleShape).background(Color.White),
            contentAlignment = Alignment.Center,
        ) { content() }
    }
}

@Composable
internal fun BackButton(onBack: () -> Unit) {
    CircleButton(onBack) {
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = stringResource(R.string.cd_back),
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(26.dp),
        )
    }
}

/**
 * Save toggle. Saving pops the heart (a spring from 0.8, interruptible, skipped when animations
 * are off) with a haptic in the same frame; unsaving gets the haptic only.
 */
@Composable
internal fun HeartButton(saved: Boolean, name: String, onToggle: () -> Unit) {
    val animate = LocalAnimationsEnabled.current
    val haptics = LocalHapticFeedback.current
    val pop = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    CircleButton(
        onClick = {
            haptics.performHapticFeedback(if (saved) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn)
            if (!saved && animate) {
                scope.launch {
                    pop.snapTo(0.8f)
                    pop.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium))
                }
            }
            onToggle()
        },
    ) {
        Icon(
            imageVector = if (saved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = stringResource(if (saved) R.string.saved_cd_unsave else R.string.saved_cd_save, name),
            tint = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp).graphicsLayer { scaleX = pop.value; scaleY = pop.value },
        )
    }
}

// ---------- Loading and not found ----------

/** Static blocks (no shimmer, like Home), shown only while the spot loads. */
@Composable
internal fun LoadingContent(heroHeight: Dp, modifier: Modifier = Modifier) {
    val block = MaterialTheme.colorScheme.surfaceContainerHigh
    val loading = stringResource(R.string.details_loading)
    Column(Modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = loading }) {
        Box(Modifier.fillMaxWidth().height(heroHeight).background(block))
        Column(modifier.padding(horizontal = ContentMargin, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.fillMaxWidth(0.75f).height(28.dp).clip(CircleShape).background(block))
            Box(Modifier.fillMaxWidth(0.4f).height(14.dp).clip(CircleShape).background(block))
            Box(Modifier.width(72.dp).height(26.dp).clip(CircleShape).background(block))
        }
    }
}

@Composable
internal fun NotFoundContent(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = ContentMargin),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_restaurant), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.details_not_found_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.details_not_found_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = onBack, shape = CircleShape) { Text(stringResource(R.string.details_go_back)) }
    }
}

// ---------- Photo viewer ----------

/** Full screen on black, photo fitted (never cropped). Back or the X closes it. */
@Composable
internal fun PhotoViewer(@DrawableRes photo: Int, name: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        LaunchedEffect(window) {
            window?.let {
                WindowCompat.getInsetsController(it, it.decorView).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            Image(
                painter = painterResource(photo),
                contentDescription = stringResource(R.string.details_cd_photo, name),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
            )
            IconButton(onClick = onDismiss, modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing).padding(4.dp)) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.details_close_photo), tint = Color.White)
            }
        }
    }
}

// ---------- Directions ----------

/**
 * Google Maps with a route from the user's position to the spot. Falls back to any app that
 * opens the link (browser). Returns false when nothing can open it.
 */
internal fun openDirections(context: Context, latitude: Double, longitude: Double): Boolean {
    val uri = "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude".toUri()
    val intent = Intent(Intent.ACTION_VIEW, uri)
    return try {
        context.startActivity(Intent(intent).setPackage("com.google.android.apps.maps"))
        true
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }
}
