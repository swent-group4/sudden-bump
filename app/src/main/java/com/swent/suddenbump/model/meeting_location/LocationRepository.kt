package com.swent.suddenbump.model.meeting_location
/**
 * Interface defining the contract for a location repository.
 *
 * Implementations of this interface are responsible for performing location searches
 * and returning the results or handling errors accordingly.
 */
interface LocationRepository {
    /**
     * Searches for locations based on the provided query string.
     *
     * @param query The search query string (e.g., an address or place name).
     * @param onSuccess A callback invoked with a list of [Location] objects when the search is successful.
     * @param onFailure A callback invoked with an [Exception] if the search fails.
     */
    fun search(query: String, onSuccess: (List<Location>) -> Unit, onFailure: (Exception) -> Unit)
}