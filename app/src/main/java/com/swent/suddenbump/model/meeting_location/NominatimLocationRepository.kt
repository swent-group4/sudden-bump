package com.swent.suddenbump.model.meeting_location

import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class NominatimLocationRepository(val client: OkHttpClient) : LocationRepository {
    override fun search(
        query: String,
        onSuccess: (List<Location>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val url = "https://nominatim.openstreetmap.org/search?q=$query&format=json"
        val request = Request.Builder().url(url).build()

        client
            .newCall(request)
            .enqueue(
                object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        onFailure(e)
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        response.body?.let { responseBody ->
                            val json = responseBody.string()
                            val locations = parseLocations(json)
                            onSuccess(locations)
                        } ?: onFailure(IOException("Empty response body"))
                    }
                })
    }

    private fun parseLocations(json: String): List<Location> {
        val jsonArray = JSONArray(json)
        val locations = mutableListOf<Location>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val location =
                Location(
                    name = jsonObject.getString("display_name"),
                    latitude = jsonObject.getDouble("lat"),
                    longitude = jsonObject.getDouble("lon"))
            locations.add(location)
        }
        return locations
    }
}