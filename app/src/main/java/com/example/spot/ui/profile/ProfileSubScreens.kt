package com.example.spot.ui.profile

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.spot.R
import com.example.spot.location.LocationPermissions
import com.example.spot.ui.motion.LocalAnimationsEnabled
import kotlinx.coroutines.launch

/** PLACEHOLDER: replace with your support address before release. */
const val SupportEmail = "support@example.com"

private val CardShape = RoundedCornerShape(16.dp)

// ---------- Shared layout ----------

/** Back arrow, title, scrolling content. Used by App Settings, About and Help (no bottom bar). */
@Composable
internal fun SubScreenLayout(
    @StringRes title: Int,
    onBack: () -> Unit,
    snackbarHost: SnackbarHostState? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                }
                Text(
                    text = stringResource(title),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
                content = content,
            )
        }
        snackbarHost?.let { SnackbarHost(it, Modifier.align(Alignment.BottomCenter).padding(8.dp)) }
    }
}

internal fun appVersionName(context: Context): String {
    val pm = context.packageManager
    val info = if (Build.VERSION.SDK_INT >= 33) {
        pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION") pm.getPackageInfo(context.packageName, 0)
    }
    return info.versionName.orEmpty()
}

// ---------- App Settings ----------

enum class LocationAccess(@param:StringRes val label: Int, @param:StringRes val detail: Int) {
    Precise(R.string.settings_location_precise, R.string.settings_location_precise_detail),
    Approximate(R.string.settings_location_approximate, R.string.settings_location_approximate_detail),
    Off(R.string.settings_location_off, R.string.settings_location_off_detail),
}

private fun locationAccess(context: Context) = when {
    LocationPermissions.isPrecise(context) -> LocationAccess.Precise
    LocationPermissions.isGranted(context) -> LocationAccess.Approximate
    else -> LocationAccess.Off // denied and "don't ask again" look the same from here: the fix is Settings either way
}

@Composable
fun AppSettingsRoute(onBack: () -> Unit) {
    val context = LocalContext.current
    var access by remember { mutableStateOf(locationAccess(context)) }
    LifecycleResumeEffect(Unit) { // back from system Settings: show the new status
        access = locationAccess(context)
        onPauseOrDispose { }
    }
    AppSettingsScreen(
        access = access,
        version = remember { appVersionName(context) },
        onOpenSystemSettings = { context.startActivity(LocationPermissions.appSettingsIntent(context)) },
        onBack = onBack,
    )
}

@Composable
fun AppSettingsScreen(access: LocationAccess, version: String, onOpenSystemSettings: () -> Unit, onBack: () -> Unit) {
    SubScreenLayout(R.string.settings_title, onBack) {
        Column(
            Modifier.fillMaxWidth().shadow(1.dp, CardShape).clip(CardShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest).padding(16.dp),
        ) {
            Text(stringResource(R.string.settings_location_heading), style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(access.label),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = if (access == LocationAccess.Off) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            Text(stringResource(access.detail), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onOpenSystemSettings) { Text(stringResource(R.string.settings_open_system)) }
        }
        Spacer(Modifier.weight(1f).heightIn(min = 24.dp))
        Text(
            text = stringResource(R.string.settings_version, version),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        )
    }
}

// ---------- About ----------

@Composable
fun AboutRoute(onBack: () -> Unit) {
    val context = LocalContext.current
    AboutScreen(version = remember { appVersionName(context) }, onBack = onBack)
}

@Composable
fun AboutScreen(version: String, onBack: () -> Unit) {
    SubScreenLayout(R.string.about_title, onBack) {
        Column(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(104.dp).clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.primary).padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(painterResource(R.drawable.ic_spot_pin), contentDescription = null, contentScale = ContentScale.Fit)
                Image(
                    painterResource(R.drawable.ic_spot_utensils), contentDescription = null,
                    modifier = Modifier.size(38.dp).align(Alignment.TopCenter).padding(top = 6.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Image(
                painter = painterResource(R.drawable.ic_spot_wordmark),
                contentDescription = stringResource(R.string.app_name),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary), // the wordmark is white for the splash
                modifier = Modifier.width(140.dp),
                contentScale = ContentScale.FillWidth,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.about_tagline),
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.settings_version, version), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.about_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ---------- Help & Support ----------

private val Faqs = listOf(
    R.string.help_q_save to R.string.help_a_save,
    R.string.help_q_distance to R.string.help_a_distance,
    R.string.help_q_directions to R.string.help_a_directions,
    R.string.help_q_missing to R.string.help_a_missing,
)

@Composable
fun HelpRoute(onBack: () -> Unit) {
    val context = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val noEmailApp = stringResource(R.string.help_no_email_app, SupportEmail)
    val subject = stringResource(R.string.help_email_subject, appVersionName(context))
    HelpScreen(
        snackbarHost = snackbarHost,
        onBack = onBack,
        onContactSupport = {
            val draft = Intent(Intent.ACTION_SENDTO, "mailto:".toUri())
                .putExtra(Intent.EXTRA_EMAIL, arrayOf(SupportEmail))
                .putExtra(Intent.EXTRA_SUBJECT, subject)
            try {
                context.startActivity(draft)
            } catch (_: ActivityNotFoundException) {
                scope.launch { snackbarHost.showSnackbar(noEmailApp) }
            }
        },
    )
}

@Composable
fun HelpScreen(snackbarHost: SnackbarHostState, onBack: () -> Unit, onContactSupport: () -> Unit) {
    SubScreenLayout(R.string.help_title, onBack, snackbarHost) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Faqs.forEach { (q, a) -> FaqItem(stringResource(q), stringResource(a)) }
        }
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.help_contact_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onContactSupport, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.help_contact)) }
        Spacer(Modifier.height(80.dp)) // room for the snackbar
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    var expanded by rememberSaveable(question) { mutableStateOf(false) }
    val animate = LocalAnimationsEnabled.current
    val state = stringResource(if (expanded) R.string.help_expanded else R.string.help_collapsed)
    Column(Modifier.fillMaxWidth().shadow(1.dp, CardShape).clip(CardShape).background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(role = Role.Button) { expanded = !expanded }
                .semantics { stateDescription = state }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(question, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
            Icon(if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, contentDescription = null)
        }
        AnimatedVisibility(
            visible = expanded,
            enter = if (animate) expandVertically() + fadeIn() else EnterTransition.None,
            exit = if (animate) shrinkVertically() + fadeOut() else ExitTransition.None,
        ) {
            Text(
                text = answer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            )
        }
    }
}
