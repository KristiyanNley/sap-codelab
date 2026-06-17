package com.sap.codelab.utils.location

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

internal data class NominatimPlace(
    val displayName: String,
    val lat: Double,
    val lng: Double
)

internal object NominatimService {

    private const val BASE_URL = "https://nominatim.openstreetmap.org/search"
    private const val USER_AGENT = "SAPCodelab/1.0"
    private const val SUGGESTIONS_LIMIT = 5
    private const val NETWORK_TIMEOUT_MS = 5_000
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_LAT = "lat"
    private const val KEY_LON = "lon"

    suspend fun getSuggestions(query: String, packageName: String): List<NominatimPlace> =
        withContext(Dispatchers.IO) {
            runCatching {
                val encoded = URLEncoder.encode(query, "UTF-8")
                val lang = Locale.getDefault().language
                val url = URL("$BASE_URL?q=$encoded&format=json&limit=$SUGGESTIONS_LIMIT&accept-language=$lang")

                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("User-Agent", "$USER_AGENT ($packageName)")
                conn.connectTimeout = NETWORK_TIMEOUT_MS
                conn.readTimeout = NETWORK_TIMEOUT_MS

                val body = conn.inputStream.bufferedReader().readText()
                parseResponse(body)
            }.getOrDefault(emptyList())
        }

    private fun parseResponse(json: String): List<NominatimPlace> {
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            NominatimPlace(
                displayName = obj.getString(KEY_DISPLAY_NAME),
                lat = obj.getString(KEY_LAT).toDouble(),
                lng = obj.getString(KEY_LON).toDouble()
            )
        }
    }
}
