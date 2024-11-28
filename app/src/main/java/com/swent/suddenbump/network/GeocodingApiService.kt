package com.swent.suddenbump.network

import com.swent.suddenbump.model.geocoding.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiService {
  @GET("geocode/json")
  suspend fun reverseGeocode(
      @Query("latlng") latlng: String,
      @Query("key") apiKey: String
  ): GeocodingResponse
}
