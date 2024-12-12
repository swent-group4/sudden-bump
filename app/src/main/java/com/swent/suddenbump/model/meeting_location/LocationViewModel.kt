package com.swent.suddenbump.model.meeting_location

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swent.suddenbump.BuildConfig
import com.swent.suddenbump.model.geocoding.Result
import com.swent.suddenbump.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

/**
 * ViewModel for managing location search and suggestions using a [LocationRepository].
 *
 * @param repository The repository that handles location search operations.
 */
class LocationViewModel : ViewModel() {

    private val _locationSuggestions = mutableStateOf<List<Result>>(emptyList())
    val locationSuggestions: State<List<Result>> = _locationSuggestions

    fun fetchLocationSuggestions(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.geocodingApi.geocode(query, BuildConfig.MAPS_API_KEY)
                if (response.status == "OK") {
                    _locationSuggestions.value = response.results
                } else {
                    Log.e("LocationViewModel", "Failed to fetch locations: ${response.status}")
                }
            } catch (e: Exception) {
                Log.e("LocationViewModel", "Error fetching locations", e)
            }
        }
    }
}

