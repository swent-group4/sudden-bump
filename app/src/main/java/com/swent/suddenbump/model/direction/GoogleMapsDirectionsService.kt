package com.swent.suddenbump.model.direction

import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleMapsDirectionsService {
    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String, // Format : "latitude,longitude"
        @Query("destination") destination: String, // Format : "latitude,longitude"
        @Query("mode") mode: String, // Exemples : "driving", "walking", "transit"
        @Query("key") apiKey: String
    ): DirectionsResponse
}
