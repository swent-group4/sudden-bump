package com.swent.suddenbump.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.swent.suddenbump.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class SignUpScreenTest {

  private val navigationActions = mock(NavigationActions::class.java)
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    composeTestRule.setContent { SignUpScreen(navigationActions) }
  }

  @Test
  fun testSignUpScreen() {
    // Check if all UI elements are displayed
    composeTestRule.onNodeWithTag("profilePictureButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("firstNameField").assertIsDisplayed()
    composeTestRule.onNodeWithText("First Name").assertIsDisplayed()
    composeTestRule.onNodeWithTag("lastNameField").assertIsDisplayed()
    composeTestRule.onNodeWithText("Last Name").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emailField").assertIsDisplayed()
    composeTestRule.onNodeWithText("Email").assertIsDisplayed()
    composeTestRule.onNodeWithTag("phoneField").assertIsDisplayed()
    composeTestRule.onNodeWithText("Phone Number").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createAccountButton").assertIsDisplayed()
    composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()

    // Perform text input
    composeTestRule.onNodeWithTag("firstNameField").performTextInput("John")
    composeTestRule.onNodeWithTag("lastNameField").performTextInput("Doe")
    composeTestRule.onNodeWithTag("emailField").performTextInput("john.doe@example.com")
    composeTestRule.onNodeWithTag("phoneField").performTextInput("+1234567890")

    // Check visual transformation
    composeTestRule.onNodeWithTag("phoneField").assertTextContains("+1 234-567-890")
    composeTestRule.onNodeWithTag("phoneField").performTextClearance()
    composeTestRule.onNodeWithTag("phoneField").performTextInput("+33123456789")
    composeTestRule.onNodeWithTag("phoneField").assertTextContains("+33 1 23 45 67 89")
    composeTestRule.onNodeWithTag("phoneField").performTextClearance()
    composeTestRule.onNodeWithTag("phoneField").performTextInput("+41791234567")
    composeTestRule.onNodeWithTag("phoneField").assertTextContains("+41 79 123 45 67")

    // Perform button click
    composeTestRule.onNodeWithTag("profilePictureButton").performClick()
    //        composeTestRule.onNodeWithTag("noProfilePic").assertIsDisplayed()

    // Perform create account button click
    //    composeTestRule.onNodeWithTag("createAccountButton").performClick()
    //    verify(navigationActions).navigateTo(Screen.OVERVIEW)
  }
}
