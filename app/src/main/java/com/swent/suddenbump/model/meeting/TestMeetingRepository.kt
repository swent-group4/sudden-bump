package com.swent.suddenbump.model.meeting

import com.google.firebase.Timestamp

class TestMeetingRepository : MeetingRepository {
  private val meetings =
      mutableListOf(
          Meeting(meetingId = "1", location = "Cafe", date = Timestamp.now(), friendId = "1"),
          Meeting(meetingId = "2", location = "Office", date = Timestamp.now(), friendId = "2"))

  override fun getNewMeetingId(): String {
    return (meetings.size + 1).toString()
  }

  override fun init(onSuccess: () -> Unit) {
    onSuccess()
  }

  override fun getMeetings(onSuccess: (List<Meeting>) -> Unit, onFailure: (Exception) -> Unit) {
    onSuccess(meetings)
  }

  override fun addMeeting(meeting: Meeting, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    meetings.add(meeting)
    onSuccess()
  }

  override fun updateMeeting(
      meeting: Meeting,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val index = meetings.indexOfFirst { it.meetingId == meeting.meetingId }
    if (index != -1) {
      meetings[index] = meeting
      onSuccess()
    } else {
      onFailure(Exception("Meeting not found"))
    }
  }

  override fun deleteMeetingById(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val index = meetings.indexOfFirst { it.meetingId == id }
    if (index != -1) {
      meetings.removeAt(index)
      onSuccess()
    } else {
      onFailure(Exception("Meeting not found"))
    }
  }
}
