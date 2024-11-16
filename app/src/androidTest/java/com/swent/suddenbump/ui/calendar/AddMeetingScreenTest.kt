package com.swent.suddenbump.ui.calendar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.meeting.MeetingRepository
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull

@RunWith(AndroidJUnit4::class)
class AddMeetingScreenTest {

  private lateinit var navigationActions: NavigationActions

  private lateinit var meetingViewModel: MeetingViewModel
  private lateinit var meetingRepository: MeetingRepository

  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var chatRepository: ChatRepository

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    meetingRepository = mock(MeetingRepository::class.java)
    userRepository = mock(UserRepository::class.java)
    chatRepository = mock(ChatRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    meetingViewModel = MeetingViewModel(meetingRepository)
    userViewModel = UserViewModel(userRepository, chatRepository)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.ADD_MEETING)
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent {
      AddMeetingScreen(navigationActions, userViewModel, meetingViewModel)
    }
    // Check top app bar title
    composeTestRule.onNodeWithTag("Add New Meeting").assertIsDisplayed()
    // Check back button
    composeTestRule.onNodeWithTag("Back").assertIsDisplayed()
    // Input location
    composeTestRule.onNodeWithTag("Location").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Location").performTextInput("Central Park")
    // Input date
    composeTestRule.onNodeWithTag("Date").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Date").performTextInput("25/12/2024")
    // Click save button
    composeTestRule.onNodeWithTag("Save Meeting").assertIsDisplayed()
  }

  @Test
  fun doesNotSubmitWithInvalidDate() {
    composeTestRule.setContent {
      AddMeetingScreen(navigationActions, userViewModel, meetingViewModel)
    }

    composeTestRule.onNodeWithTag("Date").performTextClearance()
    composeTestRule.onNodeWithTag("Date").performTextInput("notadate")
    composeTestRule.onNodeWithTag("Save Meeting").performClick()

    verify(meetingRepository, never()).addMeeting(anyOrNull(), anyOrNull(), anyOrNull())
  }

  @Test
  fun saveButton_savesMeeting() {
    `when`(meetingRepository.getNewMeetingId()).thenReturn("mockedMeetingId")
    // Arrange
    composeTestRule.setContent {
      AddMeetingScreen(navigationActions, userViewModel, meetingViewModel)
    }

    // Act
    composeTestRule.onNodeWithTag("Location").performTextInput("Central Park")
    composeTestRule.onNodeWithTag("Date").performTextInput("05/09/2024")
    composeTestRule.onNodeWithTag("Save Meeting").performClick()
    composeTestRule.waitForIdle() // Ensure all actions are processed

    // Assert: Check addMeeting is called with any arguments
    verify(meetingRepository).addMeeting(any(), any(), any())
  }
}
