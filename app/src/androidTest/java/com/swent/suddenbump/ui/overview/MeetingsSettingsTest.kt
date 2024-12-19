package com.swent.suddenbump.ui.overview

import android.location.Location
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MeetingsSettingsTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var meetingViewModel: MeetingViewModel
  private lateinit var userViewModel: UserViewModel

  @Before
  fun setUp() {
    meetingViewModel = mockk(relaxed = true)
    userViewModel = mockk(relaxed = true)

    // Mock current user
    every { userViewModel.getCurrentUser() } returns
        MutableStateFlow(
            User(
                uid = "testUserId",
                firstName = "John",
                lastName = "Doe",
                phoneNumber = "+123456789",
                profilePicture = null,
                emailAddress = "john.doe@example.com",
                lastKnownLocation = Location("mock_provider")))
  }

  @Test
  fun deleteAllMeetingsButton_displaysDialogAndPerformsDeletion() {
    composeTestRule.setContent {
      DeleteAllMeetingsItem(meetingViewModel = meetingViewModel, userViewModel = userViewModel)
    }

    // Verify the delete button is displayed
    composeTestRule.onNodeWithTag("DeleteButton").assertIsDisplayed()

    // Click the delete button
    composeTestRule.onNodeWithTag("DeleteButton").performClick()

    // Verify the dialog appears
    composeTestRule.onNodeWithTag("deleteAllMeetingsDialog").assertIsDisplayed()

    composeTestRule.onNodeWithTag("AreYouSureDeleteText").assertIsDisplayed()

    // Click "Yes" to confirm
    composeTestRule.onNodeWithText("Yes").performClick()

    // Verify the deleteMeetingsForUser function is called with the correct userId
    verify { meetingViewModel.deleteMeetingsForUser("testUserId") }

    // Verify the dialog is dismissed
    composeTestRule.onNodeWithTag("deleteAllMeetingsDialog").assertDoesNotExist()
  }

  @Test
  fun deleteAllMeetingsDialog_dismissesOnNoClick() {
    composeTestRule.setContent {
      DeleteAllMeetingsItem(meetingViewModel = meetingViewModel, userViewModel = userViewModel)
    }

    // Click the delete button to show the dialog
    composeTestRule.onNodeWithTag("DeleteButton").performClick()

    // Verify the dialog appears
    composeTestRule.onNodeWithTag("deleteAllMeetingsDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteAllMeetingsTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AreYouSureDeleteText").assertIsDisplayed()

    // Click "No" to dismiss the dialog
    composeTestRule.onNodeWithText("No").performClick()

    // Verify the dialog is dismissed
    composeTestRule.onNodeWithTag("deleteAllMeetingsDialog").assertDoesNotExist()

    // Verify the deleteMeetingsForUser function is NOT called
    verify(exactly = 0) { meetingViewModel.deleteMeetingsForUser(any()) }
  }
}
