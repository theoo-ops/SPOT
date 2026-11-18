package com.example.spot.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object LocationPermissions {
    val all = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    fun isGranted(context: Context) = isPrecise(context) ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun isPrecise(context: Context) =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun appSettingsIntent(context: Context) =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun locationSettingsIntent() =
        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}

enum class PermissionOutcome { Precise, Approximate, Denied, DeniedPermanently }

/**
 * Returns a function that shows the system location dialog (fine + coarse).
 * "Don't ask again" is detected after a denial: the system no longer wants a rationale.
 */
@Composable
fun rememberLocationPermissionRequest(onResult: (PermissionOutcome) -> Unit): () -> Unit {
    val activity = LocalActivity.current
    val latestOnResult by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        val outcome = when {
            grants[Manifest.permission.ACCESS_FINE_LOCATION] == true -> PermissionOutcome.Precise
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> PermissionOutcome.Approximate
            activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.ACCESS_FINE_LOCATION,
            ) -> PermissionOutcome.DeniedPermanently
            else -> PermissionOutcome.Denied
        }
        latestOnResult(outcome)
    }
    return { launcher.launch(LocationPermissions.all) }
}
