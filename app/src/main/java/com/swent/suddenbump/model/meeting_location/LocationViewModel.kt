package com.swent.suddenbump.model.meeting_location

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
        viewModelScope.launch {
            repository.search(
                query,
                onSuccess = { locations -> _locationSuggestions.value = locations },
                onFailure = { exception ->
                    // Handle error
                })
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                ): T {
                    return LocationViewModel(NominatimLocationRepository(OkHttpClient())) as T
                }
            }
    }
}