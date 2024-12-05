package com.swent.suddenbump.model.direction

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DirectionsRepository {

    private val directionsService: GoogleMapsDirectionsService

    init {
        // Initialiser Retrofit ici
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        directionsService = retrofit.create(GoogleMapsDirectionsService::class.java)
    }

    suspend fun getDirections(
        origin: String,
        destination: String,
        mode: String,
        apiKey: String
    ): DirectionsResponse? {
        return withContext(Dispatchers.IO) {
            try {
                directionsService.getDirections(origin, destination, mode, apiKey)
            } catch (e: Exception) {
                // Gérer les exceptions si nécessaire
                null
            }
        }
    }
}