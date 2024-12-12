package com.swent.suddenbump.model.geocoding

import com.google.maps.model.Geometry

data class GeocodingResponse(val results: List<Result>, val status: String)

data class Result(
    val address_components: List<AddressComponent>,
    val formatted_address: String,
    val place_id: String,
    val types: List<String>,
    val geometry: Geometry
)

data class AddressComponent(val long_name: String, val short_name: String, val types: List<String>)

data class Geometry(
    val location: Location // Add location to get latitude and longitude
)

data class Location(
    val lat: Double,
    val lng: Double
)
