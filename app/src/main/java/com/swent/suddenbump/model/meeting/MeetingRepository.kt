package com.swent.suddenbump.model.meeting

import com.swent.suddenbump.model.user.User

interface MeetingRepository {
    fun init(onSuccess: () -> Unit)

    fun getMeetings(user: User, onSuccess: (List<Meeting>) -> Unit, onFailure: (Exception) -> Unit)

    fun addMeeting(meeting: Meeting, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    fun updateMeeting(meeting: Meeting, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    fun deleteMeetingById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}