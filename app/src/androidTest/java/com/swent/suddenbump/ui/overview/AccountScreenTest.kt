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

    // Verify that each section is displayed
    composeTestRule.onNodeWithText("Birthday").assertIsDisplayed()
    composeTestRule.onNodeWithText("Language").assertIsDisplayed()
    composeTestRule.onNodeWithText("Delete Account").assertIsDisplayed()
    composeTestRule.onNodeWithText("Log out").assertIsDisplayed()
  }

  @Test
  fun goBackButtonCallsNavActions() {
    // Perform a click on the back button and verify that the goBack navigation action is triggered
    composeTestRule.onNodeWithTag("backButton").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun birthdayButtonNavigatesToAccountScreen() {
    // Perform a click on the "Birthday" section and verify navigation to "AccountScreen"
    composeTestRule.onNodeWithText("Birthday").performClick()
    verify(navigationActions).navigateTo("AccountScreen")
  }

  @Test
  fun languageButtonNavigatesToAccountScreen() {
    // Perform a click on the "Language" section and verify navigation to "AccountScreen"
    composeTestRule.onNodeWithText("Language").performClick()
    verify(navigationActions).navigateTo("AccountScreen")
  }

  @Test
  fun deleteAccountButtonNavigatesToDeleteAccount() {
    // Perform a click on the "Delete Account" section and verify navigation to "Delete Account"
    composeTestRule.onNodeWithText("Delete Account").performClick()
    verify(navigationActions).navigateTo("Delete Account")
  }

  @Test
  fun logOutButtonNavigatesToLogOut() {
    // Perform a click on the "Log out" section and verify navigation to "Log out"
    composeTestRule.onNodeWithText("Log out").performClick()
    verify(navigationActions).navigateTo("Log out")
  }
}
