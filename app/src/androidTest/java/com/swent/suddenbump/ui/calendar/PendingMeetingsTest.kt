package com.swent.suddenbump.ui.calendar

import android.location.Location
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.swent.suddenbump.model.meeting.Meeting
import com.swent.suddenbump.model.meeting.MeetingRepository
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PendingMeetingsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions

  private lateinit var meetingViewModel: MeetingViewModel
  private lateinit var meetingRepository: MeetingRepository

  private lateinit var userViewModel: UserViewModel

  @Before
  fun setUp() {
    navigationActions = mockk(relaxed = true)
    userViewModel = mockk(relaxed = true)
    meetingRepository = mockk(relaxed = true)
    meetingViewModel = MeetingViewModel(meetingRepository)

    // Mock meetings
    val meeting1 =
        Meeting(
            "1", com.swent.suddenbump.model.meeting_location.Location(12.34, 56.78, "Central Park"), Timestamp(Date(1735403269000)), "currentUserId", "creator1", false)
    val meeting2 =
        Meeting(
            "2", com.swent.suddenbump.model.meeting_location.Location(12.24, 56.78, "City Square"), Timestamp(Date(1735403269000)), "currentUserId", "creator2", true)

    val mockMeetings = listOf(meeting1, meeting2)

    every { meetingRepository.getMeetings(any(), any()) } answers
        {
          val onSuccess = firstArg<(List<Meeting>) -> Unit>()
          onSuccess(mockMeetings)
        }

    // Mock user friends and current user
    val user1 =
        User(
            "creator1",
            "Mike",
            "Tyson",
            "+12345678",
            null,
            "user1@email.com",
            MutableStateFlow(Location("mock_provider")))
    val user2 =
        User(
            "creator2",
            "Joe",
            "Biden",
            "+123456789",
            null,
            "user2@email.com",
            MutableStateFlow(Location("mock_provider")))

    every { userViewModel.getUserFriends() } returns MutableStateFlow(listOf(user1, user2))
    every { userViewModel.getCurrentUser() } returns
        MutableStateFlow(
            User(
                "currentUserId",
                "Jake",
                "Paul",
                "+1234567890",
                null,
                "current@gmail.com",
                MutableStateFlow(Location("mock_provider"))))
  }

  @Test
  fun displayPendingMeetingsScreen() {
    composeTestRule.setContent {
      PendingMeetingsScreen(navigationActions, meetingViewModel, userViewModel)
    }

    // Verify top app bar
    composeTestRule.onNodeWithTag("Pending Meetings").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Back").assertIsDisplayed()

    // Verify bottom navigation
    composeTestRule.onNodeWithTag("pendingMeetingsScreen").assertIsDisplayed()

    // Verify meetings list
    composeTestRule.onNodeWithTag("pendingMeetingRow").assertIsDisplayed()
  }

  @Test
  fun acceptMeeting_callsUpdateMeeting() {
    val meetingAccepted =
        Meeting(
            "1", com.swent.suddenbump.model.meeting_location.Location(12.34, 56.78, "Central Park"), Timestamp(Date(1735403269000)), "currentUserId", "creator1", true)

    composeTestRule.setContent {
      PendingMeetingsScreen(navigationActions, meetingViewModel, userViewModel)
    }
    // Verify accept button is displayed
    composeTestRule.onNodeWithTag("acceptButton").assertIsDisplayed()

    // Simulate clicking the accept button
    composeTestRule.onNodeWithTag("acceptButton").performClick()

    // Verify that updateMeeting was called with the correct arguments
    verify { meetingRepository.updateMeeting(eq(meetingAccepted), any(), any()) }
  }

  @Test
  fun declineMeeting_callsDeleteMeeting() {
    composeTestRule.setContent {
      PendingMeetingsScreen(navigationActions, meetingViewModel, userViewModel)
    }
    // Verify deny button is displayed
    composeTestRule.onNodeWithTag("denyButton").assertIsDisplayed()

    // Simulate clicking the decline button
    composeTestRule.onNodeWithTag("denyButton").performClick()

    // Verify that deleteMeeting was called with the correct ID
    verify { meetingRepository.deleteMeetingById(eq("1"), any(), any()) }
  }

  @Test
  fun backButton_callsNavigationGoBack() {
    composeTestRule.setContent {
      PendingMeetingsScreen(navigationActions, meetingViewModel, userViewModel)
    }

    // Simulate clicking the back button
    composeTestRule.onNodeWithContentDescription("Back").performClick()

    // Verify that navigationActions.goBack() was called
    verify { navigationActions.goBack() }
  }

  @Test
  fun pendingMeetingRow_displaysCorrectDetails() {
    composeTestRule.setContent {
      PendingMeetingsScreen(navigationActions, meetingViewModel, userViewModel)
    }

    // Verify the userName text field
    composeTestRule
        .onNodeWithTag("userName", useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Mike T.")
        .assertIsDisplayed()

    // Verify the meetingDetails text field
    composeTestRule
        .onNodeWithTag("meetingDetails", useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Meet at Central Park on 2024-12-28")
        .assertIsDisplayed()

    // Verify the profile image
    composeTestRule
        .onNodeWithTag("profileImage", useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()

    // Verify the divider
    composeTestRule
        .onNodeWithTag("divider", useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
  }
}
