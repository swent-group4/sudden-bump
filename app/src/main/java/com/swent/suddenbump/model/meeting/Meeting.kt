package com.swent.suddenbump.model.meeting

import com.google.firebase.Timestamp

data class Meeting(
    val userId: String = "",
    val location: String = "",
    val date: Timestamp = Timestamp.now(),
    val friendId: String = ""
)