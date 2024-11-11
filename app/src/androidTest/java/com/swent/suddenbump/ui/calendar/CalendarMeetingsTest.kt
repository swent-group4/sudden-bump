package com.swent.suddenbump.ui.calendar

import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.meeting.MeetingRepository
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.meeting.TestMeetingRepositoryHelper
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class CalendarMeetingsTest {
  private lateinit var navigationActions: NavigationActions

  private lateinit var meetingViewModel: MeetingViewModel
  private lateinit var userRepository: UserRepository
  private lateinit var meetingRepository: MeetingRepository

  private lateinit var userFriendsFlow: StateFlow<List<User>>

  @Mock private lateinit var userViewModel: UserViewModel
  @Mock private lateinit var chatRepository: ChatRepository

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    navigationActions = mock(NavigationActions::class.java)
    userRepository = mock(UserRepository::class.java)
    chatRepository = mock(ChatRepository::class.java)
    userViewModel = spy(UserViewModel(userRepository, chatRepository))
    `when`(navigationActions.currentRoute()).thenReturn(Route.CALENDAR)

    meetingRepository = TestMeetingRepositoryHelper()
    meetingViewModel = MeetingViewModel(meetingRepository)

    /*userFriendsFlow =
       MutableStateFlow(
           listOf(
               User(
                   uid = "1",
                   firstName = "John",
                   lastName = "Doe",
                   phoneNumber = "1234567890",
                   profilePicture = null,
                   emailAddress = "john.doe@example.com",
                   lastKnownLocation = GeoLocation(0.0, 0.0)),
               User(
                   uid = "2",
                   firstName = "Jane",
                   lastName = "Smith",
                   phoneNumber = "0987654321",
                   profilePicture = null,
                   emailAddress = "jane.smith@example.com",
                   lastKnownLocation = GeoLocation(0.0, 0.0))))

    */

    // doReturn(userFriendsFlow).`when`(userViewModel).getUserFriends()

    composeTestRule.setContent {
      CalendarMeetingsScreen(navigationActions, meetingViewModel, userViewModel)
    }
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.onNodeWithTag("calendarMeetingsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("monthYearHeader").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("dayRow")[0].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("dayRow")[1].assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }

  @Test
  fun dayRowDisplaysMeetings() {
    composeTestRule.onAllNodesWithTag("dayRow")[0].assertIsDisplayed()

    composeTestRule.onAllNodesWithTag("meetText")[0].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("meetText")[1].assertIsDisplayed()
  }

  @Test
  fun dayRowDisplaysNoMeetingsMessage() {
    composeTestRule.onAllNodesWithTag("dayRow")[1].assertIsDisplayed()
    composeTestRule
        .onAllNodesWithText("No meetings for this day")
        .assertAny(hasText("No meetings for this day"))
  }
}
