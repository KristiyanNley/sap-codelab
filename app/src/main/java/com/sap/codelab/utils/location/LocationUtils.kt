package com.sap.codelab.utils.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

internal object LocationUtils {

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

    @SuppressLint("MissingPermission")
    suspend fun getDistanceMeters(context: Context, targetLat: Double, targetLng: Double): Int? =
        withContext(Dispatchers.IO) {
            runCatching {
                val userLocation = suspendCancellableCoroutine<Location?> { cont ->
                    LocationServices.getFusedLocationProviderClient(context)
                        .lastLocation
                        .addOnSuccessListener { cont.resume(it) }
                        .addOnFailureListener { cont.resume(null) }
                } ?: return@withContext null

                val results = FloatArray(1)
                Location.distanceBetween(
                    userLocation.latitude, userLocation.longitude,
                    targetLat, targetLng,
                    results
                )
                results[0].toInt()
            }.getOrNull()
        }

    fun formatDistance(meters: Int): String = when {
        meters < 1000 -> "${meters}m away"
        else -> "${"%.1f".format(meters / 1000.0)}km away"
    }
}