package com.swent.suddenbump.ui.calendar

import android.location.Location
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.swent.suddenbump.model.meeting.Meeting
import com.swent.suddenbump.model.meeting.MeetingRepository
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import com.swent.suddenbump.ui.navigation.Screen
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarMeetingsScreenTest {

  private lateinit var navigationActions: NavigationActions
  private lateinit var meetingViewModel: MeetingViewModel
  private lateinit var userViewModel: UserViewModel
  private lateinit var meetingRepository: MeetingRepository

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mockk(relaxed = true)
    userViewModel = mockk(relaxed = true)
    meetingRepository = mockk(relaxed = true)
    meetingViewModel = MeetingViewModel(meetingRepository)

    // Mock user data
    val currentUser =
        User(
            uid = "currentUserId",
            firstName = "Jake",
            lastName = "Paul",
            phoneNumber = "+1234567890",
            profilePicture = null,
            emailAddress = "current@gmail.com",
            lastKnownLocation = MutableStateFlow(Location("mock_provider")))
    val userFriends =
        listOf(
            User(
                "friendId1",
                "Mike",
                "Tyson",
                "+12345678",
                null,
                "mike@example.com",
                MutableStateFlow(Location("mock_provider"))),
            User(
                "friendId2",
                "Joe",
                "Biden",
                "+123456789",
                null,
                "joe@example.com",
                MutableStateFlow(Location("mock_provider"))))

    every { userViewModel.getUserFriends() } returns MutableStateFlow(userFriends)
    every { userViewModel.getCurrentUser() } returns MutableStateFlow(currentUser)
    // Mock meeting data
    val meetings =
        listOf(
            Meeting("1", com.swent.suddenbump.model.meeting_location.Location(12.34, 56.78, "Central Park"), Timestamp.now(), "currentUserId", "friendId1", false),
            Meeting("2", com.swent.suddenbump.model.meeting_location.Location(12.24, 56.78, "City Square"), Timestamp.now(), "currentUserId", "friendId2", true))
    every { meetingRepository.getMeetings(any(), any()) } answers
        {
          val onSuccess = firstArg<(List<Meeting>) -> Unit>()
          onSuccess(meetings)
        }

    // Mock navigation actions
    every { navigationActions.currentRoute() } returns (Route.OVERVIEW)
    every { navigationActions.goBack() } returns Unit
    every { navigationActions.navigateTo(Screen.EDIT_MEETING) } returns Unit

    composeTestRule.setContent {
      CalendarMeetingsScreen(navigationActions, meetingViewModel, userViewModel)
    }
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.onNodeWithTag("calendarMeetingsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("monthYearHeader", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }

  @Test
  fun displaysPendingMeetingsBadgeWithCorrectCount() {
    val pendingMeetingsCount = 1 // Based on mock data
    composeTestRule.onNodeWithText(pendingMeetingsCount.toString()).assertIsDisplayed()
  }

  @Test
  fun dayRowDisplaysNoMeetingsMessage() {
    composeTestRule.onAllNodesWithTag("dayRow")[1].assertIsDisplayed()
    composeTestRule
        .onAllNodesWithText("No meetings for this day")
        .assertAny(hasText("No meetings for this day"))
  }

  @Test
  fun displaysMeetingsForSpecificDay() {
    // Verify that the first dayRow is displayed
    composeTestRule.onAllNodesWithTag("dayRow").onFirst().assertIsDisplayed()

    // Use `useUnmergedTree` to find the node with `meetText`
    composeTestRule
        .onNodeWithTag("meetText", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextContains("Meet Joe Biden at City Square")
  }

  @Test
  fun clickingMeetingNavigatesToEditMeeting() {
    // Simulate clicking a meeting in the first "dayRow"
    composeTestRule.onAllNodesWithTag("dayRow").onFirst().performClick()

    // Verify the navigation action was triggered
    verify(exactly = 1) { navigationActions.navigateTo(Screen.EDIT_MEETING) }
  }

  @Test
  fun asyncImageIsDisplayedForMeeting() {
    composeTestRule.onNodeWithTag("profileImage", useUnmergedTree = true).assertIsDisplayed()
  }
}
