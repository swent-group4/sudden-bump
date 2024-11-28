package com.swent.suddenbump.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val GEOCODING_BASE_URL = "https://maps.googleapis.com/maps/api/"

    private val client = OkHttpClient.Builder()
        // Optionally add logging or other interceptors
        .build()

    val geocodingApi: GeocodingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(GEOCODING_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeocodingApiService::class.java)
    }
}
