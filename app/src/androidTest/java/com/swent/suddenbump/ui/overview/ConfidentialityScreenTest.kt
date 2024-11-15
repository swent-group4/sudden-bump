package com.swent.suddenbump.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class ConfidentialityScreenTest {

  private lateinit var navigationActions: NavigationActions
  private lateinit var userViewModel: UserViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    userViewModel = mock(UserViewModel::class.java)

    composeTestRule.setContent {
      ConfidentialityScreen(navigationActions = navigationActions, userViewModel = userViewModel)
    }
  }
  /*
  @Test
  fun hasRequiredComponents() {
      composeTestRule.onNodeWithText("Confidentiality Settings").assertIsDisplayed()
      composeTestRule.onNodeWithTag("onlinePresence").assertIsDisplayed()
      composeTestRule.onNodeWithTag("lastTimeOnline").assertIsDisplayed()
      composeTestRule.onNodeWithTag("whenOnline").assertIsDisplayed()
      composeTestRule.onNodeWithTag("profilePhoto").assertIsDisplayed()
      composeTestRule.onNodeWithTag("whoCanSeeProfilePhoto").assertIsDisplayed()
      composeTestRule.onNodeWithTag("myInfo").assertIsDisplayed()
      composeTestRule.onNodeWithTag("whoCanSeeMyInfo").assertIsDisplayed()
      composeTestRule.onNodeWithTag("groups").assertIsDisplayed()
      composeTestRule.onNodeWithTag("whoCanAddToGroups").assertIsDisplayed()
      composeTestRule.onNodeWithTag("status").assertIsDisplayed()
      composeTestRule.onNodeWithTag("whoCanSeeStatus").assertIsDisplayed()
      composeTestRule.onNodeWithTag("blockedContacts").assertIsDisplayed()
      composeTestRule.onNodeWithTag("showBlockedContactsButton").assertIsDisplayed()
      composeTestRule.onNodeWithTag("locationStatus").assertIsDisplayed()
  }*/

  @Test
  fun goBackButtonCallsNavActions() {
    composeTestRule.onNodeWithTag("backButton").performClick()
    verify(navigationActions).goBack()
  }
  /*
  @Test
  fun showBlockedContactsButtonWorks() {
      composeTestRule.onNodeWithTag("showBlockedContactsButton").assertIsDisplayed()
      composeTestRule.onNodeWithTag("showBlockedContactsButton").performClick()
      // Add any further assertions if needed for what happens after click
  }*/

  @Test
  fun onlinePresenceOptionsWorks() {
    composeTestRule.onNodeWithTag("onlinePresenceOptions").assertIsDisplayed()
    composeTestRule.onNodeWithTag("onlinePresenceOptions").performClick()
    // Additional assertions can be made here based on the selected state.
  }

  @Test
  fun profilePhotoOptionsWorks() {
    composeTestRule.onNodeWithTag("profilePhotoOptions").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profilePhotoOptions").performClick()
    composeTestRule.waitForIdle() // Wait for UI to settle after the click
    // Additional assertions to check option selection if needed.
  }

  /*
  @Test
  fun myInfoOptionsWorks() {
      composeTestRule.onNodeWithTag("myInfoOptions").assertIsDisplayed()
      composeTestRule.onNodeWithTag("myInfoOptions").performClick()
      // Additional assertions to check option selection if needed.
  }
  */
  /*
  @Test
  fun groupsOptionsWorks() {
      composeTestRule.onNodeWithTag("groupsOptions").assertIsDisplayed()
      composeTestRule.onNodeWithTag("groupsOptions").performClick()
      // Additional assertions to check option selection if needed.
  }*/
  /*
  @Test
  fun statusOptionsWorks() {
      composeTestRule.onNodeWithTag("statusOptions").assertIsDisplayed()
      composeTestRule.onNodeWithTag("statusOptions").performClick()
      // Additional assertions to check option selection if needed.
  }

  @Test
  fun locationStatusOptionsWorks() {
      composeTestRule.onNodeWithTag("locationStatusOptions").assertIsDisplayed()
      composeTestRule.onNodeWithTag("locationStatusOptions").performClick()
      // Additional assertions to check option selection if needed.
  }*/
}
