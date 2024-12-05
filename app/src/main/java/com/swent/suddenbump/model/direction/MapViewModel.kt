package com.swent.suddenbump.model.direction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapViewModel(
    private val directionsRepository: DirectionsRepository
) : ViewModel() {

    private val _polylinePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val polylinePoints: StateFlow<List<LatLng>> = _polylinePoints

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun fetchDirections(origin: LatLng, destination: LatLng, mode: String, apiKey: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = directionsRepository.getDirections(
                    origin = "${origin.latitude},${origin.longitude}",
                    destination = "${destination.latitude},${destination.longitude}",
                    mode = mode,
                    apiKey = apiKey
                )
                val points = decodePolyline(response?.routes?.firstOrNull()?.overview_polyline?.points.orEmpty())
                _polylinePoints.value = points
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f).toInt() shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) {
                (result shr 1).inv()
            } else {
                result shr 1
            }
            lat += dlat.toInt() // Ensure 'dlat' is Int


            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f).toInt() shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) {
                (result shr 1).inv()
            } else {
                (result shr 1).inv()
            }
            lng += dlng.toInt() // Ensure 'dlng' is Int

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }

}
