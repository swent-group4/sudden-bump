package com.swent.suddenbump.model.meeting_location

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class LocationViewModel(val repository: LocationRepository) : ViewModel() {
    private val _locationSuggestions = MutableStateFlow<List<Location>>(emptyList())
    val locationSuggestions: StateFlow<List<Location>> = _locationSuggestions

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    fun setQuery(query: String) {
        _query.value = query
        searchLocations(query)
    }

    private fun searchLocations(query: String) {
        if (query.isBlank()) {
            _locationSuggestions.value = emptyList()
            return
        }

        viewModelScope.launch {
            repository.search(
                query,
                onSuccess = { locations ->
                    Log.d("LocationViewModel", "Search successful with ${locations.size} results")
                    _locationSuggestions.value = locations
                },
                onFailure = { exception ->
                    Log.e("LocationViewModel", "Error searching locations", exception)
                }
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                ): T {
                    val client = OkHttpClient.Builder()
                        .addInterceptor { chain ->
                            val request = chain.request()
                                .newBuilder()
                                .addHeader("User-Agent", "SuddenBump")
                                .build()
                            chain.proceed(request)
                        }
                        .build()
                    return LocationViewModel(NominatimLocationRepository(client)) as T
                }
            }
    }
}
