/*package com.swent.suddenbump.ui.settings

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.overview.SettingsScreen
import com.swent.suddenbump.model.user.UserViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
      SettingsScreen(navigationActions, userViewModel)
    }
  }

  @Test
  fun testProfilePictureIsDisplayed() {
    composeTestRule.onNodeWithTag("profilePicture").assertExists()
  }

  @Test
  fun testAddPhotoButtonIsClickable() {
    composeTestRule.onNodeWithTag("addPhotoButton").assertExists().assertIsEnabled().performClick()
  }

  @Test
  fun testNotificationsSwitchTogglesCorrectly() {
    val switch = composeTestRule.onNodeWithTag("notificationsSwitch")
    switch.assertExists().assertIsOn().performClick().assertIsOff().performClick().assertIsOn()
  }

  @Test
  fun testAccountPrivacySwitchTogglesCorrectly() {
    val switch = composeTestRule.onNodeWithTag("accountPrivacySwitch")
    switch.assertExists().assertIsOff().performClick().assertIsOn()
  }

  @Test
  fun testChangePasswordButtonIsClickable() {
    composeTestRule.onNodeWithTag("changePasswordButton").assertExists().assertIsEnabled().performClick()
  }

  @Test
  fun testLogoutButtonIsClickable() {
    composeTestRule.onNodeWithTag("logoutButton").assertExists().assertIsEnabled().performClick()
  }

  @Test
  fun testHelpButtonIsClickable() {
    composeTestRule.onNodeWithTag("helpButton").assertExists().assertIsEnabled().performClick()
  }

  @Test
  fun testInviteFriendButtonIsClickable() {
    composeTestRule.onNodeWithTag("inviteFriendButton").assertExists().assertIsEnabled().performClick()
  }

  @Test
  fun testImportantMessagesButtonIsClickable() {
    composeTestRule.onNodeWithTag("importantMessagesButton").assertExists().assertIsEnabled().performClick()
  }

  @Test
  fun testGoBackButtonIsClickable() {
    composeTestRule.onNodeWithTag("customGoBackButton").assertExists().performClick()
  }

  @Test
  fun testLanguageDropdownSelectionChanges() {
    composeTestRule.onNodeWithTag("languageButton").performClick()
    composeTestRule.onNodeWithText("French").performClick()
    composeTestRule.onNodeWithTag("languageButton").assertTextContains("French")
  }
}*/
