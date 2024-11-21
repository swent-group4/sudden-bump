package com.swent.suddenbump.ui.calendar

import android.location.Location
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.meeting.Meeting
import com.swent.suddenbump.model.meeting.MeetingRepository
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class PendingMeetingsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions

  private lateinit var meetingViewModel: MeetingViewModel
  private lateinit var meetingRepository: MeetingRepository

  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var chatRepository: ChatRepository

  @Before
  fun setUp() {
    meetingRepository = mock(MeetingRepository::class.java)
    userRepository = mock(UserRepository::class.java)
    chatRepository = mock(ChatRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    meetingViewModel = MeetingViewModel(meetingRepository)
    userViewModel = UserViewModel(userRepository, chatRepository)

    // Mock StateFlow data for meetings
    val meetingsFlow =
        MutableStateFlow(
            listOf(
                Meeting("1", "Central Park", Timestamp.now(), "friend1", "creator1", false),
                Meeting("2", "City Square", Timestamp.now(), "friend1", "creator2", false)))
    val mockStateFlow = mock(StateFlow::class.java) as StateFlow<List<Meeting>>
    //        `when`(meetingViewModel.meetings).thenReturn(mockStateFlow)
    // doReturn(mockStateFlow).`when`(meetingViewModel.meetings)
    /* `when`(mockStateFlow.value).thenReturn(listOf(
        Meeting("1", "Central Park", Timestamp.now(), "friend1", "creator1", false),
        Meeting("2", "City Square", Timestamp.now(), "friend1", "creator2", false)
    ))*/
    //        `when`(meetingViewModel.meetings).thenReturn(meetingsFlow)

    // Mock StateFlow data for user friends
    val userFriendsFlow =
        MutableStateFlow(
            listOf(
                User(
                    "creator1",
                    "Mike",
                    "Tyson",
                    "12345678",
                    null,
                    "user@email.com",
                    MutableStateFlow(Location("default"))),
                User(
                    "creator2",
                    "Joe",
                    "Biden",
                    "12345678",
                    null,
                    "user@email.com",
                    MutableStateFlow(Location("default")))))
    // `when`(userViewModel.getUserFriends()).thenReturn(userFriendsFlow)

    // Mock StateFlow data for current user
    val currentUserFlow =
        MutableStateFlow(
            User(
                "friend1",
                "John",
                "Doe",
                "12345678",
                null,
                "user@email.com",
                MutableStateFlow(Location("default"))))
    // `when`(userViewModel.getCurrentUser()).thenReturn(currentUserFlow)

    // Mock navigation actions
    `when`(navigationActions.currentRoute()).thenReturn(Screen.PENDING_MEETINGS)
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
    // composeTestRule.onAllNodesWithTag("pendingMeetingRow").assertCountEquals(2)
  }
  /*
  @Test
  fun acceptMeeting_callsUpdateMeeting() {
      composeTestRule.setContent {
          PendingMeetingsScreen(
              navigationActions = navigationActions,
              meetingViewModel = meetingViewModel,
              userViewModel = userViewModel
          )
      }

      // Click accept button
      composeTestRule.onAllNodesWithTag("acceptButton")[0].performClick()

      // Verify updateMeeting called with correct arguments
      verify(meetingViewModel).updateMeeting(any())
  }

  @Test
  fun declineMeeting_callsDeleteMeeting() {
      composeTestRule.setContent {
          PendingMeetingsScreen(
              navigationActions = navigationActions,
              meetingViewModel = meetingViewModel,
              userViewModel = userViewModel
          )
      }

      // Click decline button
      composeTestRule.onAllNodesWithTag("denyButton")[0].performClick()

      // Verify deleteMeeting called with correct arguments
      verify(meetingViewModel).deleteMeeting(anyOrNull())
  }

  @Test
  fun backButton_callsNavigationGoBack() {
      composeTestRule.setContent {
          PendingMeetingsScreen(
              navigationActions = navigationActions,
              meetingViewModel = meetingViewModel,
              userViewModel = userViewModel
          )
      }

      // Click back button
      composeTestRule.onNodeWithContentDescription("Back").performClick()

      // Verify goBack called
      verify(navigationActions).goBack()
  }*/
}
