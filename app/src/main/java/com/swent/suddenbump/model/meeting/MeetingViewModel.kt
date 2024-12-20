package com.swent.suddenbump.model.meeting

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing meetings.
 *
 * @param repositoryMeeting The repository for accessing meeting data.
 */
open class MeetingViewModel(private val repositoryMeeting: MeetingRepository) : ViewModel() {

  private val _meetings = MutableStateFlow<List<Meeting>>(emptyList())
  val meetings: StateFlow<List<Meeting>> = _meetings.asStateFlow()
  private val _error = MutableStateFlow<Exception?>(null)
  val selectedMeeting = MutableStateFlow<Meeting?>(null)

  companion object {
    /** Factory for creating instances of MeetingViewModel. */
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MeetingViewModel(MeetingRepositoryFirestore(Firebase.firestore)) as T
          }
        }
  }
  /**
   * Generates a new unique meeting ID.
   *
   * @return A new unique meeting ID as a String.
   */
  fun getNewMeetingid(): String {
    return repositoryMeeting.getNewMeetingId()
  }

  init {
    repositoryMeeting.init { Log.i("MeetingViewModel", "Repository initialized") }
    fetchMeetings()
  }

  /** Fetches the list of meetings from the repository. */
  private fun fetchMeetings() {
    viewModelScope.launch { getMeetings() }
  }
  /**
   * Adds a new meeting to the repository.
   *
   * @param meeting The meeting to be added.
   */
  fun addMeeting(meeting: Meeting) {
    repositoryMeeting.addMeeting(meeting, onSuccess = { fetchMeetings() }, onFailure = {})
  }
  /** Retrieves the list of meetings from the repository. */
  fun getMeetings() {
    repositoryMeeting.getMeetings(
        onSuccess = { meetingsList ->
          Log.i(
              "MeetingViewModel",
              "Meetings retrieved successfully: ${meetingsList.size} meetings found")
          meetingsList.forEach { meeting ->
            Log.i(
                "MeetingViewModel",
                "Meeting details - ID: ${meeting.meetingId}, Friend ID: ${meeting.friendId}, Location: ${meeting.location}, Date: ${meeting.date}")
          }
          _meetings.value = meetingsList
        },
        onFailure = { exception ->
          Log.e("MeetingViewModel", "Failed to retrieve meetings: ${exception.message}")
          _error.value = exception
        })
  }
  /**
   * Updates an existing meeting in the repository.
   *
   * @param meeting The meeting to be updated.
   */
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
  /**
   * Deletes a meeting from the repository by its ID.
   *
   * @param id The ID of the meeting to be deleted.
   */
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
  /**
   * Selects a meeting to be the currently active meeting.
   *
   * @param meeting The meeting to be selected.
   */
  fun selectMeeting(meeting: Meeting) {
    selectedMeeting.value = meeting
  }

  /** Deletes all expired meetings from the repository. */
  fun deleteExpiredMeetings() {
    val currentDate = Calendar.getInstance().time
    meetings.value.forEach { meeting ->
      if (meeting.date.toDate().before(currentDate)) {
        deleteMeeting(meeting.meetingId)
      }
    }
  }

  /**
   * Deletes all meetings for a given user ID.
   *
   * @param userId The ID of the user whose meetings are to be deleted.
   */
  fun deleteMeetingsForUser(userId: String) {
    viewModelScope.launch {
      meetings.value.forEach { meeting ->
        if (meeting.friendId == userId || meeting.creatorId == userId) {
          deleteMeeting(meeting.meetingId)
        }
      }
    }
  }
  /**
   * Deletes all meetings with a specific friend ID.
   *
   * @param friendId The ID of the friend whose meetings are to be deleted.
   * @param currentUserID The ID of the current user.
   */
  fun deleteAllMeetingsWithSpecificFriend(friendId: String, currentUserID: String) {
    viewModelScope.launch {
      meetings.value.forEach { meeting ->
        if ((meeting.friendId == friendId && meeting.creatorId == currentUserID) ||
            (meeting.friendId == currentUserID && meeting.creatorId == friendId)) {
          deleteMeeting(meeting.meetingId)
        }
      }
    }
  }
}
