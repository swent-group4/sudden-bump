package com.swent.suddenbump.model.meeting

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swent.suddenbump.model.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MeetingViewModel(private val repositoryMeeting: MeetingRepository, private val repositoryUser: UserRepository) : ViewModel() {


    private val _meetings = MutableStateFlow<List<Meeting>>(emptyList())
    private val _error = MutableStateFlow<Exception?>(null)
    val selectedMeeting = MutableStateFlow<Meeting?>(null)

    init {
        repositoryMeeting.init { Log.i("MeetingViewModel", "Repository initialized") }
        fetchMeetings()
    }


    private fun fetchMeetings() {
        viewModelScope.launch {
            getMeetings()
        }
    }


    fun addMeeting(meeting: Meeting) {
        repositoryMeeting.addMeeting(meeting, onSuccess = { fetchMeetings() }, onFailure = {})
    }


    fun getMeetings() {
        repositoryUser.getUserAccount(
            onSuccess = { user ->
                repositoryMeeting.getMeetings(
                    user,
                    onSuccess = { meetingsList ->
                        Log.i("MeetingsUserViewModel", "Meetings retrieved successfully: ${meetingsList.size} meetings found")
                        meetingsList.forEach { meeting ->
                            Log.i("MeetingsUserViewModel", "Meeting details - ID: ${meeting.userId}, Friend ID: ${meeting.friendId}, Location: ${meeting.location}, Date: ${meeting.date}")
                        }
                        _meetings.value = meetingsList
                    },
                    onFailure = { exception ->
                        Log.e("MeetingsUserViewModel", "Failed to retrieve meetings: ${exception.message}")
                        _error.value = exception
                    }
                )
            },
            onFailure = { exception ->
                _error.value = exception
            }
        )
    }

    fun updateMeeting(meeting: Meeting) {
        viewModelScope.launch {
            repositoryMeeting.updateMeeting(
                meeting,
                onSuccess = { fetchMeetings() },
                onFailure = { exception ->
                    Log.e("MeetingViewModel", "Error updating meeting", exception)
                }
            )
        }
    }

    fun deleteMeeting(id: String) {
        viewModelScope.launch {
            repositoryMeeting.deleteMeetingById(
                id,
                onSuccess = { fetchMeetings() },
                onFailure = { exception ->
                    Log.e("MeetingViewModel", "Error deleting meeting", exception)
                }
            )
        }
    }

    fun selectMeeting(meeting: Meeting) {
        selectedMeeting.value = meeting
    }
}