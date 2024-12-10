package com.swent.suddenbump.model.meeting_location

import android.util.Log
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import java.net.URLEncoder

/**
 * A repository class that interfaces with the Nominatim API to perform location searches.
 *
 * @param client An instance of [OkHttpClient] used for making HTTP requests.
 */
class NominatimLocationRepository(val client: OkHttpClient) : LocationRepository {
    /**
     * Searches for locations matching the given query using the Nominatim API.
     *
     * @param query The search query (e.g., an address or place name).
     * @param onSuccess A callback invoked with the list of [Location] objects upon a successful search.
     * @param onFailure A callback invoked with an [Exception] if the search fails.
     */
    override fun search(
        query: String,
        onSuccess: (List<Location>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json"
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "SuddenBump")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("Nominatim", "Network request failed", e)
                onFailure(e)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                try {
                    if (!response.isSuccessful) {
                        onFailure(IOException("Unexpected HTTP code ${response.code}"))
                        return
                    }

                    val responseBody = response.body?.string()
                    if (responseBody.isNullOrEmpty()) {
                        onFailure(IOException("Empty response body"))
                        return
                    }

                    if (responseBody.trim().startsWith("[")) {
                        val locations = parseLocations(responseBody)
                        onSuccess(locations)
                    } else {
                        onSuccess(emptyList()) // Pass an empty list on parsing failure
                    }
                } catch (e: Exception) {
                    Log.e("Nominatim", "Error processing response", e)
                    onFailure(e)
                }
            }
        })
    }
    /**
     * Parses a JSON response from the Nominatim API into a list of [Location] objects.
     *
     * @param json The JSON string to parse.
     * @return A list of [Location] objects, or an empty list if parsing fails.
     */
    private fun parseLocations(json: String): List<Location> {
        return try {
            val jsonArray = JSONArray(json)
            val locations = mutableListOf<Location>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val location = Location(
                    name = jsonObject.optString("display_name", "Unknown"),
                    latitude = jsonObject.optDouble("lat", Double.NaN),
                    longitude = jsonObject.optDouble("lon", Double.NaN)
                )
                locations.add(location)
            }
            locations
        } catch (e: JSONException) {
            Log.e("Nominatim", "Error parsing locations", e)
            emptyList()
        }
    }
}

