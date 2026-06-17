package com.sap.codelab.utils.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.sap.codelab.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

internal object LocationUtils {

    suspend fun searchLocation(context: Context, query: String): Pair<Double, Double>? =
        withContext(Dispatchers.IO) {
            runCatching {
                val geocoder = Geocoder(context, Locale.getDefault())
                val results: List<Address> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocationName(query, 1) { addresses ->
                            cont.resume(addresses)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(query, 1) ?: emptyList()
                }
                results.firstOrNull()?.let { Pair(it.latitude, it.longitude) }
            }.getOrNull()
        }

    suspend fun getAddressFromCoordinates(context: Context, lat: Double, lng: Double): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val geocoder = Geocoder(context, Locale.getDefault())
                val address: Address? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(lat, lng, 1) { results ->
                            cont.resume(results.firstOrNull())
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()
                }
                address?.let { formatAddress(it) }
            }.getOrNull()
        }

    private fun formatAddress(address: Address): String {
        val parts = listOfNotNull(
            buildString {
                address.subThoroughfare?.takeIf { it.isNotBlank() }?.let { append("$it ") }
                address.thoroughfare?.takeIf { it.isNotBlank() }?.let { append(it) }
            }.takeIf { it.isNotBlank() },
            address.locality,
            address.adminArea,
            address.countryName
        )
        return parts.joinToString(", ")
    }

    fun formatDistance(context: Context, meters: Int): String = when {
        meters < 1000 -> context.getString(R.string.distance_meters, meters)
        else -> context.getString(R.string.distance_kilometers, meters / 1000.0f)
    }
}