package com.swent.suddenbump.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.suddenbump.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class AccountScreenTest {

  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    composeTestRule.setContent { AccountScreen(navigationActions = navigationActions) }
  }

  @Test
  fun hasRequiredComponents() {
    // Verify that the top bar title "Account" is displayed
    composeTestRule.onNodeWithText("Account").assertIsDisplayed()

    // Verify that each key section is displayed
    composeTestRule.onNodeWithTag("languageSection").assertIsDisplayed()
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
  fun languageButtonOpensLanguageMenu() {
    // Perform a click on the "Language" section
    composeTestRule.onNodeWithTag("languageSection").performClick()

    // Verify that the dropdown menu appears
    composeTestRule.onNodeWithTag("languageMenuItem_English").assertIsDisplayed()
    composeTestRule.onNodeWithTag("languageMenuItem_French").assertIsDisplayed()
    composeTestRule.onNodeWithTag("languageMenuItem_German").assertIsDisplayed()
  }

  @Test
  fun deleteAccountButtonNavigatesToAccountScreen() {
    // Perform a click on the "Delete Account" section
    composeTestRule.onNodeWithTag("deleteAccountSection").performClick()
    verify(navigationActions).navigateTo("AccountScreen")
  }

  @Test
  fun logOutButtonNavigatesToAccountScreen() {
    // Perform a click on the "Log out" section
    composeTestRule.onNodeWithTag("logoutSection").performClick()
    verify(navigationActions).navigateTo("AccountScreen")
  }
}
