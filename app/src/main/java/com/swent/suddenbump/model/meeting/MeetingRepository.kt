package com.swent.suddenbump.model.meeting

interface MeetingRepository {

    fun getNewMeetingId(): String

    fun init(onSuccess: () -> Unit)

    fun getMeetings(onSuccess: (List<Meeting>) -> Unit, onFailure: (Exception) -> Unit)

    fun addMeeting(meeting: Meeting, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    fun updateMeeting(meeting: Meeting, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    fun deleteMeetingById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}