package com.sap.codelab.view.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sap.codelab.R
import com.sap.codelab.model.NearbyPlace

private val ColorPrimary = Color(0xFF9CCC65)
private val ColorPrimaryDark = Color(0xFF6B9B37)
private val ColorPrimaryContainer = Color(0xFFDCEDC8)

@Composable
internal fun NearbyPlacesSection(
    state: NearbyPlacesUiState,
    onRequestLoad: () -> Unit,
    onPlaceSelected: (NearbyPlace) -> Unit,
    modifier: Modifier = Modifier
) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = ColorPrimary,
            onPrimary = Color.White,
            primaryContainer = ColorPrimaryContainer,
            onPrimaryContainer = ColorPrimaryDark
        )
    ) {
        Column(modifier = modifier.fillMaxWidth()) {
            NearbyPlacesHeader(
                showFindButton = state is NearbyPlacesUiState.Idle,
                onFindClick = onRequestLoad
            )
            when (state) {
                is NearbyPlacesUiState.Idle -> Unit
                is NearbyPlacesUiState.Loading -> LoadingContent()
                is NearbyPlacesUiState.Success -> PlacesList(
                    places = state.places,
                    onPlaceSelected = onPlaceSelected
                )
                is NearbyPlacesUiState.Error -> ErrorContent(onRetry = onRequestLoad)
            }
        }
    }
}

@Composable
private fun NearbyPlacesHeader(showFindButton: Boolean, onFindClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_location_pin),
            contentDescription = null,
            tint = ColorPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.nearby_places_title),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f)
        )
        if (showFindButton) {
            FilledTonalButton(
                onClick = onFindClick,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(stringResource(R.string.nearby_places_find))
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = ColorPrimary, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun PlacesList(places: List<NearbyPlace>, onPlaceSelected: (NearbyPlace) -> Unit) {
    LazyRow(
        modifier = Modifier.padding(top = 8.dp),
        contentPadding = PaddingValues(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(places) { place ->
            PlaceCard(place = place, onClick = { onPlaceSelected(place) })
        }
    }
}

@Composable
private fun PlaceCard(place: NearbyPlace, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(148.dp),
        colors = CardDefaults.cardColors(containerColor = ColorPrimaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = amenityEmoji(place.type), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                text = place.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.distance_meters, place.distanceMeters),
                style = MaterialTheme.typography.labelSmall,
                color = ColorPrimaryDark
            )
        }
    }
}

@Composable
private fun ErrorContent(onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.nearby_places_empty),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.nearby_places_retry))
        }
    }
}

private fun amenityEmoji(type: String): String = when (type) {
    "restaurant" -> "🍽️"
    "cafe" -> "☕"
    "bar", "pub" -> "🍺"
    "fast_food" -> "🍔"
    "bakery" -> "🥐"
    "pharmacy" -> "💊"
    "supermarket", "convenience" -> "🛒"
    "ice_cream" -> "🍦"
    else -> "📍"
}