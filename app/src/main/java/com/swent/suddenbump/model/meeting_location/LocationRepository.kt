package com.swent.suddenbump.model.meeting_location

interface LocationRepository {
    fun search(query: String, onSuccess: (List<Location>) -> Unit, onFailure: (Exception) -> Unit)
}