package com.swent.suddenbump.model.meeting

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class MeetingViewModel(private val repositoryMeeting: MeetingRepository) : ViewModel() {

  private val _meetings = MutableStateFlow<List<Meeting>>(emptyList())
  val meetings: StateFlow<List<Meeting>> = _meetings.asStateFlow()
  private val _error = MutableStateFlow<Exception?>(null)
  val selectedMeeting = MutableStateFlow<Meeting?>(null)

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MeetingViewModel(MeetingRepositoryFirestore(Firebase.firestore)) as T
          }
        }
  }

  fun getNewMeetingid(): String {
    return repositoryMeeting.getNewMeetingId()
  }

  init {
    repositoryMeeting.init { Log.i("MeetingViewModel", "Repository initialized") }
    fetchMeetings()
  }

  private fun fetchMeetings() {
    viewModelScope.launch { getMeetings() }
  }

  fun addMeeting(meeting: Meeting) {
    repositoryMeeting.addMeeting(meeting, onSuccess = { fetchMeetings() }, onFailure = {})
  }

  fun getMeetings() {
    repositoryMeeting.getMeetings(
        onSuccess = { meetingsList ->
          Log.i(
              "MeetingsUserViewModel",
              "Meetings retrieved successfully: ${meetingsList.size} meetings found")
          meetingsList.forEach { meeting ->
            Log.i(
                "MeetingsUserViewModel",
                "Meeting details - ID: ${meeting.meetingId}, Friend ID: ${meeting.friendId}, Location: ${meeting.location}, Date: ${meeting.date}")
          }
          _meetings.value = meetingsList
        },
        onFailure = { exception ->
          Log.e("MeetingsUserViewModel", "Failed to retrieve meetings: ${exception.message}")
          _error.value = exception
        })
  }

  fun updateMeeting(meeting: Meeting) {
    viewModelScope.launch {
      repositoryMeeting.updateMeeting(
          meeting,
          onSuccess = { fetchMeetings() },
          onFailure = { exception ->
            Log.e("MeetingViewModel", "Error updating meeting", exception)
          })
    }
  }

  fun deleteMeeting(id: String) {
    viewModelScope.launch {
      repositoryMeeting.deleteMeetingById(
          id,
          onSuccess = { fetchMeetings() },
          onFailure = { exception ->
            Log.e("MeetingViewModel", "Error deleting meeting", exception)
          })
    }
  }

  fun selectMeeting(meeting: Meeting) {
    selectedMeeting.value = meeting
  }
}
