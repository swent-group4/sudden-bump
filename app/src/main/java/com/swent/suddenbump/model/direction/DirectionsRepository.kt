package com.swent.suddenbump.model.direction

import android.util.Log

class DirectionsRepository(
    private val directionsService: GoogleMapsDirectionsService
) {
    suspend fun getDirections(
        origin: String,
        destination: String,
        mode: String,
        apiKey: String
    ): DirectionsResponse? {
        return try {
            directionsService.getDirections(origin, destination, mode, apiKey)
        } catch (e: Exception) {
            Log.e("DirectionsRepository", "Error getting directions", e)
            null
        }
    }
}
