package com.swent.suddenbump.ui.settings

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.swent.suddenbump.ui.navigation.NavigationActions
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
      SettingsScreen(navigationActions)
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

  /*  @Test
  fun testUsernameCanBeUpdated() {
      val newUsername = "NewUser456"
      composeTestRule.onNodeWithTag("usernameField").assertExists()
      composeTestRule.onNodeWithTag("usernameField").performTextInput(newUsername)
      composeTestRule.onNodeWithTag("usernameField").assertTextEquals(newUsername)
  }*/

  @Test
  fun testNotificationsSwitchTogglesCorrectly() {
    val switch = composeTestRule.onNodeWithTag("notificationsSwitch")
    switch.assertExists().assertIsOn() // Initially true
    switch.performClick().assertIsOff()
    switch.performClick().assertIsOn()
  }

  /*
  @Test
  fun testVisibilityButtonOpensDropdown() {
      composeTestRule.onNodeWithTag("visibilityButton").performClick()
      composeTestRule.onNodeWithTag("visibilityDropdown").assertExists()
      composeTestRule.onNodeWithTag("visibleForMyFriendsOption").performClick()
      composeTestRule.onNodeWithTag("selectedVisibilityText").assertTextContains("Visible for my friends")
  }


   */

  /*
  @Test
  fun testLanguageButtonOpensDropdown() {
      composeTestRule.onNodeWithTag("languageButton").performClick()
      composeTestRule.onNodeWithText("English").assertExists()
      composeTestRule.onNodeWithText("Spanish").assertExists()
      composeTestRule.onNodeWithText("French").assertExists()

      // Select a new language and verify the change
      composeTestRule.onNodeWithText("Spanish").performClick()
      composeTestRule.onNodeWithTag("languageButton").assertTextContains("Spanish")
  }


   */
  @Test
  fun testAccountPrivacySwitchTogglesCorrectly() {
    val switch = composeTestRule.onNodeWithTag("accountPrivacySwitch")
    switch.assertExists().assertIsOff()
    switch.performClick().assertIsOn()
  }
  /*
     @Test
     fun testDataUsageSliderAdjustsCorrectly() {
         val slider = composeTestRule.onNodeWithTag("dataUsageSlider")
         slider.assertExists()
         slider.performTouchInput { swipeRight() }
         composeTestRule.onNodeWithText("Data Usage Limit: 100MB").assertExists()
     }


  */

  @Test
  fun testChangePasswordButtonIsClickable() {
    composeTestRule
        .onNodeWithTag("changePasswordButton")
        .assertExists()
        .assertIsEnabled()
        .performClick()
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
    composeTestRule
        .onNodeWithTag("inviteFriendButton")
        .assertExists()
        .assertIsEnabled()
        .performClick()
  }

  @Test
  fun testImportantMessagesButtonIsClickable() {
    composeTestRule
        .onNodeWithTag("importantMessagesButton")
        .assertExists()
        .assertIsEnabled()
        .performClick()
  }

  @Test
  fun testGoBackButtonIsClickable() {
    composeTestRule.onNodeWithTag("customGoBackButton").assertExists().performClick()
  }

  /*
  @Test
  fun testSelectedVisibilityChanges() {
      composeTestRule.onNodeWithTag("selectedVisibilityText").assertTextContains("Visible for all")
      composeTestRule.onNodeWithTag("visibilityButton").performClick()
      composeTestRule.onNodeWithTag("visibleForMyContactsOption").performClick()
      composeTestRule.onNodeWithTag("selectedVisibilityText").assertTextContains("Visible for my contacts")
  }


   */

  @Test
  fun testLanguageDropdownSelectionChanges() {
    composeTestRule.onNodeWithTag("languageButton").performClick()
    composeTestRule.onNodeWithText("French").performClick()
    composeTestRule.onNodeWithTag("languageButton").assertTextContains("French")
  }
}
