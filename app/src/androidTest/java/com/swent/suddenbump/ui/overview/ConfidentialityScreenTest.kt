package com.swent.suddenbump.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

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

  @Test
  fun hasRequiredComponents() {
    // Verify that the confidentiality screen is displayed
    composeTestRule.onNodeWithTag("confidentialityScreen").assertIsDisplayed()

    // Verify that the location status options section is displayed
    composeTestRule.onNodeWithTag("locationStatusOptions").assertIsDisplayed()

    // Verify that the "Blocked Contacts" button is displayed
    composeTestRule.onNodeWithTag("showBlockedContactsButton").assertIsDisplayed()
  }

  @Test
  fun goBackButtonCallsNavActions() {
    // Perform click on the back button and verify navigation action
    composeTestRule.onNodeWithTag("backButton").performClick()
    verify(navigationActions).goBack()
  }
}
