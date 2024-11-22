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
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class CalendarMeetingsTest {
  private lateinit var navigationActions: NavigationActions

  private lateinit var meetingViewModel: MeetingViewModel
  private lateinit var userRepository: UserRepository
  private lateinit var meetingRepository: MeetingRepository

  @Mock private lateinit var userViewModel: UserViewModel
  @Mock private lateinit var chatRepository: ChatRepository

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    userRepository = mock(UserRepository::class.java)
    chatRepository = mock(ChatRepository::class.java)
    userViewModel = UserViewModel(userRepository, chatRepository)
    `when`(navigationActions.currentRoute()).thenReturn(Route.CALENDAR)

    meetingRepository = mock(MeetingRepository::class.java)
    meetingViewModel = MeetingViewModel(meetingRepository)

    composeTestRule.setContent {
      CalendarMeetingsScreen(navigationActions, meetingViewModel, userViewModel)
    }
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.onNodeWithTag("calendarMeetingsScreen").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("monthYearHeader")[0].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("monthYearHeader")[1].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("dayRow")[0].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("dayRow")[1].assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }

  @Test
  fun dayRowDisplaysNoMeetingsMessage() {
    composeTestRule.onAllNodesWithTag("dayRow")[1].assertIsDisplayed()
    composeTestRule
        .onAllNodesWithText("No meetings for this day")
        .assertAny(hasText("No meetings for this day"))
  }
}
