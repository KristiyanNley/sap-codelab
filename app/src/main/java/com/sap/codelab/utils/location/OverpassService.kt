package com.sap.codelab.utils.location

import android.location.Location
import com.sap.codelab.model.NearbyPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private const val OVERPASS_BASE_URL = "https://overpass-api.de/api/interpreter"
private const val NEARBY_MAX_DISTANCE_METERS = 300
private const val NEARBY_MIN_DISTANCE_METERS = 210
private const val OVERPASS_QUERY_TIMEOUT_SECONDS = 15
private const val OVERPASS_NETWORK_TIMEOUT_MS = 15_000
private const val AMENITY_FILTER = "restaurant|cafe|bar|pub|fast_food|bakery|pharmacy|supermarket|convenience|ice_cream"
private const val KEY_ELEMENTS = "elements"
private const val KEY_TAGS = "tags"
private const val KEY_NAME = "name"
private const val KEY_AMENITY = "amenity"
private const val KEY_LAT = "lat"
private const val KEY_LON = "lon"

internal object OverpassService {

    suspend fun getNearbyPlaces(userLat: Double, userLng: Double): List<NearbyPlace> =
        withContext(Dispatchers.IO) {
            runCatching {
                val query = buildQuery(userLat, userLng)
                val conn = (URL(OVERPASS_BASE_URL).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = OVERPASS_NETWORK_TIMEOUT_MS
                    readTimeout = OVERPASS_NETWORK_TIMEOUT_MS
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    outputStream.write("data=${URLEncoder.encode(query, "UTF-8")}".toByteArray())
                }
                parseResponse(conn.inputStream.bufferedReader().readText(), userLat, userLng)
            }.getOrDefault(emptyList())
        }

    private fun buildQuery(lat: Double, lng: Double): String = """
        [out:json][timeout:$OVERPASS_QUERY_TIMEOUT_SECONDS];
        node["amenity"~"$AMENITY_FILTER"](around:$NEARBY_MAX_DISTANCE_METERS,$lat,$lng);
        out body;
    """.trimIndent()

    private fun parseResponse(json: String, userLat: Double, userLng: Double): List<NearbyPlace> {
        val elements = JSONObject(json).getJSONArray(KEY_ELEMENTS)
        return (0 until elements.length())
            .mapNotNull { i ->
                val element = elements.getJSONObject(i)
                val tags = element.optJSONObject(KEY_TAGS) ?: return@mapNotNull null
                val name = tags.optString(KEY_NAME).takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val placeLat = element.getDouble(KEY_LAT)
                val placeLng = element.getDouble(KEY_LON)
                val results = FloatArray(1)
                Location.distanceBetween(userLat, userLng, placeLat, placeLng, results)
                val distance = results[0].toInt()
                if (distance < NEARBY_MIN_DISTANCE_METERS) return@mapNotNull null
                NearbyPlace(
                    name = name,
                    type = tags.optString(KEY_AMENITY),
                    latitude = placeLat,
                    longitude = placeLng,
                    distanceMeters = distance
                )
            }
            .sortedBy { it.distanceMeters }
    }
}