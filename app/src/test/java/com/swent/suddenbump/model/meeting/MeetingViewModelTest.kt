package com.swent.suddenbump.model.meeting

import com.google.firebase.Timestamp
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class MeetingViewModelTest {
  private lateinit var meetingRepository: MeetingRepository
  private lateinit var meetingViewModel: MeetingViewModel

  private val testDispatcher = StandardTestDispatcher()

  private val meeting =
      Meeting(
          meetingId = "JhXlhoSvTmbtTFSVpNnA",
          location = "Cafe",
          date = Timestamp(Date(1725494400000)),
          friendId = "FPHuqGkCBo7Iinbo5OO9",
          creatorId = "P7vuP4bbEQB03OSR3QwJ",
          accepted = false)

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
    meetingRepository = mock(MeetingRepository::class.java)
    meetingViewModel = MeetingViewModel(meetingRepository)
    Dispatchers.setMain(testDispatcher)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun getNewUid() {
    `when`(meetingRepository.getNewMeetingId()).thenReturn("meetingId")
    assertThat(meetingViewModel.getNewMeetingid(), `is`("meetingId"))
  }

  @Test
  fun getMeetingsCallsRepository() {
    // Call the method under test
    meetingViewModel.getMeetings()

    // Verify that getMeetings is called exactly twice (once during initialization and once during
    // the test)
    verify(meetingRepository, times(2)).getMeetings(any(), any())
  }

  @Test
  fun addMeetingCallsRepository() {
    meetingViewModel.addMeeting(meeting)
    verify(meetingRepository).addMeeting(eq(meeting), any(), any())
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun deleteMeeting_callsRepositoryDelete() = runTest {
    meetingViewModel.deleteMeeting("JhXlhoSvTmbtTFSVpNnA")
    // Advance the coroutine to ensure it completes
    advanceUntilIdle()
    verify(meetingRepository).deleteMeetingById(eq("JhXlhoSvTmbtTFSVpNnA"), any(), any())
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun deleteExpiredMeetings_deletesOnlyExpiredMeetings() = runTest {
    // Create mock meetings: one expired and one not expired
    val expiredMeeting = Meeting(
      meetingId = "expiredMeetingId",
      location = "Park",
      date = Timestamp(Date(System.currentTimeMillis() - 10000)), // 10 seconds ago
      friendId = "Friend1",
      creatorId = "Creator1",
      accepted = true
    )
    val upcomingMeeting = Meeting(
      meetingId = "upcomingMeetingId",
      location = "Library",
      date = Timestamp(Date(System.currentTimeMillis() + 100000)), // 100 seconds ahead
      friendId = "Friend2",
      creatorId = "Creator2",
      accepted = false
    )

    // Mock the repository's meetings retrieval
    val mockMeetings = listOf(expiredMeeting, upcomingMeeting)
    `when`(meetingRepository.getMeetings(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (List<Meeting>) -> Unit
      onSuccess(mockMeetings) // Provide the mock meetings to the success callback
    }

    // Call getMeetings to populate the _meetings StateFlow
    meetingViewModel.getMeetings()

    // Call the method under test
    meetingViewModel.deleteExpiredMeetings()

    // Advance the coroutine to ensure it completes
    advanceUntilIdle()

    // Verify the expired meeting is deleted
    verify(meetingRepository).deleteMeetingById(eq("expiredMeetingId"), any(), any())

    // Verify the non-expired meeting is not deleted
    verify(meetingRepository, never()).deleteMeetingById(eq("upcomingMeetingId"), any(), any())
  }

}
