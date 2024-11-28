package com.swent.suddenbump.ui.calendar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.swent.suddenbump.model.meeting.Meeting
import com.swent.suddenbump.model.meeting.MeetingRepository
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq

@RunWith(AndroidJUnit4::class)
class EditMeetingScreenTest {

  private lateinit var navigationActions: NavigationActions
  private lateinit var meetingRepository: MeetingRepository
  private lateinit var meetingViewModel: MeetingViewModel
  private lateinit var viewModel: MeetingViewModel

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
    MockitoAnnotations.openMocks(this)

    meetingRepository = mock(MeetingRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    viewModel = mock(MeetingViewModel::class.java)
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
    // Edit date icon button
    composeTestRule.onNodeWithTag("DateIconButton").assertIsDisplayed()
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

  @Test
  fun saveButton_updatesMeeting() {
    // Arrange
    meetingViewModel.selectMeeting(meeting)
    composeTestRule.setContent { EditMeetingScreen(navigationActions, meetingViewModel) }

    // Act
    composeTestRule.onNodeWithTag("Location").performTextClearance()
    composeTestRule.onNodeWithTag("Location").performTextInput("New Location")
    composeTestRule.onNodeWithTag("Date").performTextClearance()
    composeTestRule.onNodeWithTag("Date").performTextInput("25/12/2024")
    composeTestRule.onNodeWithTag("Save Changes").performClick()

    // Assert
    val meetingCaptor = argumentCaptor<Meeting>()
    val successCaptor = argumentCaptor<() -> Unit>()
    val failureCaptor = argumentCaptor<(Exception) -> Unit>()
    verify(meetingRepository)
        .updateMeeting(meetingCaptor.capture(), successCaptor.capture(), failureCaptor.capture())

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val parsedDate = dateFormat.parse("25/12/2024")
    val calendar =
        GregorianCalendar().apply {
          if (parsedDate != null) {
            time = parsedDate
          }
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
    val meetingDate = Timestamp(calendar.time)

    assertEquals("JhXlhoSvTmbtTFSVpNnA", meetingCaptor.firstValue.meetingId)
    assertEquals("New Location", meetingCaptor.firstValue.location)
    assertEquals(meetingDate, meetingCaptor.firstValue.date)
    assertEquals("FPHuqGkCBo7Iinbo5OO9", meetingCaptor.firstValue.friendId)
    assertEquals("P7vuP4bbEQB03OSR3QwJ", meetingCaptor.firstValue.creatorId)
  }

  @Test
  fun deleteButton_deletesMeeting() {
    // Arrange
    meetingViewModel.selectMeeting(meeting)
    composeTestRule.setContent { EditMeetingScreen(navigationActions, meetingViewModel) }

    // Act
    composeTestRule.onNodeWithTag("Delete Meeting").performClick()

    // Assert
    val successCaptor = argumentCaptor<() -> Unit>()
    val failureCaptor = argumentCaptor<(Exception) -> Unit>()
    verify(meetingRepository)
        .deleteMeetingById(
            eq("JhXlhoSvTmbtTFSVpNnA"), successCaptor.capture(), failureCaptor.capture())
  }

  @Test
  fun doesNotSubmitWithInvalidDate() {
    // Arrange
    meetingViewModel.selectMeeting(meeting)
    composeTestRule.setContent { EditMeetingScreen(navigationActions, meetingViewModel) }

    // Test case 1: Invalid date format
    composeTestRule.onNodeWithTag("Date").performTextClearance()
    composeTestRule.onNodeWithTag("Date").performTextInput("notadate")
    composeTestRule.onNodeWithTag("Save Changes").performClick()

    verify(meetingRepository, never()).updateMeeting(anyOrNull(), anyOrNull(), anyOrNull())

    // Test case 2: Past date
    val pastDate = "01/01/2020"
    composeTestRule.onNodeWithTag("Date").performTextClearance()
    composeTestRule.onNodeWithTag("Date").performTextInput(pastDate)
    composeTestRule.onNodeWithTag("Save Changes").performClick()

    verify(meetingRepository, never()).updateMeeting(anyOrNull(), anyOrNull(), anyOrNull())

    // Test case 3: Date format with too few digits
    composeTestRule.onNodeWithTag("Date").performTextClearance()
    composeTestRule.onNodeWithTag("Date").performTextInput("12/34/2")
    composeTestRule.onNodeWithTag("Save Changes").performClick()

    verify(meetingRepository, never()).updateMeeting(anyOrNull(), anyOrNull(), anyOrNull())

    // Test case 4: Non-existent date
    composeTestRule.onNodeWithTag("Date").performTextClearance()
    composeTestRule.onNodeWithTag("Date").performTextInput("60/12/2025")
    composeTestRule.onNodeWithTag("Save Changes").performClick()

    verify(meetingRepository, never()).updateMeeting(anyOrNull(), anyOrNull(), anyOrNull())
  }
}
