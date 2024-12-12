package com.swent.suddenbump.model.meeting

import com.google.firebase.Timestamp
import com.swent.suddenbump.model.meeting_location.Location
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
          location = Location(),
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
    val expiredMeeting =
        Meeting(
            meetingId = "expiredMeetingId",
            location = Location(),
            date = Timestamp(Date(System.currentTimeMillis() - 10000)), // 10 seconds ago
            friendId = "Friend1",
            creatorId = "Creator1",
            accepted = true)
    val upcomingMeeting =
        Meeting(
            meetingId = "upcomingMeetingId",
            location = Location(),
            date = Timestamp(Date(System.currentTimeMillis() + 100000)), // 100 seconds ahead
            friendId = "Friend2",
            creatorId = "Creator2",
            accepted = false)

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

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun deleteMeetingsForUser_deletesOnlyMatchingMeetings() = runTest {
    // Mock meetings with various friendId and creatorId values
    val meeting1 =
        Meeting(
            meetingId = "meeting1",
            location = Location(),
            date = Timestamp(Date()),
            friendId = "targetUserId",
            creatorId = "Creator1",
            accepted = false)
    val meeting2 =
        Meeting(
            meetingId = "meeting2",
            location = Location(),
            date = Timestamp(Date()),
            friendId = "Friend2",
            creatorId = "targetUserId",
            accepted = true)
    val meeting3 =
        Meeting(
            meetingId = "meeting3",
            location = Location(),
            date = Timestamp(Date()),
            friendId = "Friend3",
            creatorId = "Creator3",
            accepted = true)

    val mockMeetings = listOf(meeting1, meeting2, meeting3)

    // Mock repository to provide the mock meetings
    `when`(meetingRepository.getMeetings(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[0] as (List<Meeting>) -> Unit
      onSuccess(mockMeetings)
    }

    // Call getMeetings to populate the _meetings StateFlow
    meetingViewModel.getMeetings()

    // Call the method under test with the target user ID
    meetingViewModel.deleteMeetingsForUser("targetUserId")

    // Advance the coroutine to ensure it completes
    advanceUntilIdle()

    // Verify that only the meetings with matching friendId or creatorId were deleted
    verify(meetingRepository).deleteMeetingById(eq("meeting1"), any(), any())
    verify(meetingRepository).deleteMeetingById(eq("meeting2"), any(), any())

    // Verify that the meeting with no matching userId was not deleted
    verify(meetingRepository, never()).deleteMeetingById(eq("meeting3"), any(), any())
  }
}
