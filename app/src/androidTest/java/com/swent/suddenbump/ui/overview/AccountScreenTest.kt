package com.swent.suddenbump.ui.overview

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import com.swent.suddenbump.ui.utils.isMockUsingOnlineDefaultValue
import com.swent.suddenbump.ui.utils.testableOnlineDefaultValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class AccountScreenTest {

  @Mock private lateinit var navigationActions: NavigationActions
  @Mock private lateinit var userViewModel: UserViewModel
  @Mock private lateinit var userRepository: UserRepository
  @Mock private lateinit var chatRepository: ChatRepository
  @Mock private lateinit var meetingViewModel: MeetingViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    userRepository = mock(UserRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    chatRepository = mock(ChatRepository::class.java)
    meetingViewModel = mock(MeetingViewModel::class.java)

    doNothing().`when`(userRepository).logoutUser()

    isMockUsingOnlineDefaultValue = true
    testableOnlineDefaultValue = true

    userViewModel = UserViewModel(userRepository, chatRepository)
    composeTestRule.setContent {
      AccountScreen(
          navigationActions = navigationActions,
          userViewModel = userViewModel,
          meetingViewModel = meetingViewModel)
    }
  }

  @Test
  fun hasRequiredComponents() {
    // Verify that the top bar title "Account" is displayed
    composeTestRule.onNodeWithText("Account").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteAccountSection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("logoutSection").assertIsDisplayed()
  }

  @Test
  fun goBackButtonCallsNavActions() {
    // Perform a click on the back button and verify that the goBack navigation action is triggered
    composeTestRule.onNodeWithTag("backButton").performClick()
    verify(navigationActions).goBack()
  }

  @Test

  fun deleteAccountButtonNavigatesToAccountScreen() {
    // Perform a click on the "Delete Account" section
    composeTestRule.onNodeWithTag("deleteAccountSection").performClick()
    verify(navigationActions).navigateTo("AccountScreen")


  }*/

  @Test
  fun testLogoutSectionVisibility() {
    // Assert that the logout section is displayed
    composeTestRule.onNodeWithTag("logoutSection").assertIsDisplayed()
  }

  @Test
  fun testLogoutButtonCallsLogout() {
    // Perform a click on the "Log out" section
    composeTestRule.onNodeWithTag("logoutSection").assertHasClickAction()
    composeTestRule.onNodeWithTag("logoutSection").performClick()
    verify(navigationActions).navigateTo(Route.AUTH)
  }

  @Test
  fun clickingDeleteAccountShowsConfirmationDialog() {

    // Click on the "Delete Account" section
    composeTestRule.onNodeWithTag("deleteAccountSection").performClick()

    composeTestRule.onNode(isDialog()).assertIsDisplayed()
    // The confirmation dialog should appear
    composeTestRule.onNodeWithText("Delete Account Confirmation").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Are you sure you want to delete your account?")
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Yes").assertIsDisplayed()
    composeTestRule.onNodeWithText("No").assertIsDisplayed()
  }

  @Test
  fun clickingNoOnDeleteAccountDialogDismissesIt() {
    // Show the dialog
    composeTestRule.onNodeWithTag("deleteAccountSection").performClick()

    composeTestRule.onNode(isDialog()).assertIsDisplayed()

    // Click "No" to dismiss
    composeTestRule.onNodeWithText("No").performClick()

    // The dialog should be gone now
    // Using `assertDoesNotExist` to ensure the dialog is dismissed
    composeTestRule
        .onNodeWithText("Are you sure you want to delete your account?")
        .assertDoesNotExist()
  }

  /*@Test
  fun clickingYesOnDeleteAccountDialogCallsViewModelAndNavigates() {
    // Show the dialog
    composeTestRule.onNodeWithTag("deleteAccountSection").performClick()

    composeTestRule.onNode(isDialog()).assertIsDisplayed()

    // Click "Yes" to confirm
    composeTestRule.onNodeWithText("Yes").performClick()

    // Verify that the userViewModel and meetingViewModel methods are called
    verify(meetingViewModel).deleteMeetingsForUser("testUid")
    verify(userViewModel).deleteUserAccount(navigationActions)
  }*/
}
