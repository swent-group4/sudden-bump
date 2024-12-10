package com.swent.suddenbump.model.meeting

import com.google.firebase.Timestamp
import com.swent.suddenbump.model.meeting_location.Location

data class Meeting(
    val meetingId: String = "",
    val location: Location? = Location(),
    val date: Timestamp = Timestamp.now(),
    val friendId: String = "",
    val creatorId: String = "",
    val accepted: Boolean = false
)
