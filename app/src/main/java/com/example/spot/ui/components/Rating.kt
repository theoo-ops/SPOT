package com.example.spot.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.spot.R
import com.example.spot.ui.theme.StarYellow
import java.util.Locale

/** "★ 4.6", or "New" when nobody has rated the spot yet (instead of a misleading "★ 0.0"). */
@Composable
fun RatingLabel(
    rating: Double?,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelLarge,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (rating != null) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = stringResource(R.string.cd_rating),
                tint = StarYellow,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(String.format(Locale.US, "%.1f", rating), style = style, color = MaterialTheme.colorScheme.onSurface)
        } else {
            Text(stringResource(R.string.rating_new), style = style, color = MaterialTheme.colorScheme.primary)
        }
    }
}
