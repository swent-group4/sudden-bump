package com.swent.suddenbump.ui.calendar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.swent.suddenbump.model.meeting.Meeting
import com.swent.suddenbump.model.meeting.MeetingRepository
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import java.util.Date
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class EditMeetingScreenTest {

  private lateinit var navigationActions: NavigationActions
  private lateinit var meetingRepository: MeetingRepository
  private lateinit var meetingViewModel: MeetingViewModel

  @get:Rule val composeTestRule = createComposeRule()

  private val meeting =
      Meeting(
          meetingId = "JhXlhoSvTmbtTFSVpNnA",
          location = "Cafe",
          date = Timestamp(Date(1725494400000)),
          friendId = "FPHuqGkCBo7Iinbo5OO9",
          creatorId = "P7vuP4bbEQB03OSR3QwJ")

  @Before
  fun setUp() {
    meetingRepository = mock(MeetingRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    meetingViewModel = MeetingViewModel(meetingRepository)
  }

  @Test
  fun displayAllComponents() {

    meetingViewModel.selectMeeting(meeting)
    composeTestRule.setContent { EditMeetingScreen(navigationActions, meetingViewModel) }
    // Check top app bar title
    composeTestRule.onNodeWithTag("Edit Meeting").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Edit Meeting").assertTextEquals("Edit Meeting")
    // Check back button
    composeTestRule.onNodeWithTag("Back").assertIsDisplayed()
    // Input location
    composeTestRule.onNodeWithTag("Location").assertIsDisplayed()
    // Input date
    composeTestRule.onNodeWithTag("Date").assertIsDisplayed()
    // Click save button
    composeTestRule.onNodeWithTag("Save Changes").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Save Changes").assertTextEquals("Save Changes")
    // Click delete button
    composeTestRule.onNodeWithTag("Delete Meeting").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Delete Meeting").assertTextEquals("Delete Meeting")
  }

  @Test
  fun inputsHaveInitialValue() {
    meetingViewModel.selectMeeting(meeting)
    composeTestRule.setContent { EditMeetingScreen(navigationActions, meetingViewModel) }

    Thread.sleep(10000)

    composeTestRule.onNodeWithTag("Location").assertTextContains("Cafe")
    composeTestRule.onNodeWithTag("Date").assertTextContains("05/09/2024")
  }
}