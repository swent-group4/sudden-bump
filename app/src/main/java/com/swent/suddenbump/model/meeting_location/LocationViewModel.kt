package com.swent.suddenbump.model.meeting_location

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
/**
 * ViewModel for managing location search and suggestions using a [LocationRepository].
 *
 * @param repository The repository that handles location search operations.
 */
class LocationViewModel(val repository: LocationRepository) : ViewModel() {
    private val _locationSuggestions = MutableStateFlow<List<Location>>(emptyList())
    val locationSuggestions: StateFlow<List<Location>> = _locationSuggestions

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query
    /**
     * Updates the query string and triggers a location search.
     *
     * @param query The search query string entered by the user.
     */
    fun setQuery(query: String) {
        _query.value = query
        searchLocations(query)
    }
    /**
     * Performs a location search using the provided query string.
     *
     * If the query is blank, it clears the location suggestions. Otherwise, it
     * invokes the repository to search for matching locations.
     *
     * @param query The query string for searching locations.
     */
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
    /**
     * Factory for creating instances of [LocationViewModel] with necessary dependencies.
     */
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
