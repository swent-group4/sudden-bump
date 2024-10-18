package com.swent.suddenbump

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.firebase.auth.FirebaseAuth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val activityRule = ActivityTestRule(MainActivity::class.java)

  private val auth: FirebaseAuth = FirebaseAuth.getInstance()

  @Test
  fun testFullNavigation() {
    // Launch the app
    composeTestRule.setContent {
      MainActivity() // Ensure this is your entry point
    }

    // Navigate to Sign In Screen
    composeTestRule.onNodeWithTag("loginTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").performClick()

    // Check Overview Screen
    composeTestRule.onNodeWithText("Overview").assertIsDisplayed()
    composeTestRule.onNodeWithText("Overview").performClick()

    // Navigate to Add Contact Screen
    composeTestRule.onNodeWithText("Add Contact").performClick()
    composeTestRule.onNodeWithText("Add Contact").assertIsDisplayed()

    // Navigate back to Overview Screen
    composeTestRule.onNodeWithContentDescription("Navigate up").performClick()
    composeTestRule.onNodeWithText("Overview").assertIsDisplayed()

    // Navigate to Conversation Screen
    composeTestRule.onNodeWithText("Conversations").performClick()
    composeTestRule.onNodeWithText("Conversations").assertIsDisplayed()

    // Navigate back to Overview Screen
    composeTestRule.onNodeWithContentDescription("Navigate up").performClick()
    composeTestRule.onNodeWithText("Overview").assertIsDisplayed()

    // Navigate to Settings Screen
    composeTestRule.onNodeWithText("Settings").performClick()
    composeTestRule.onNodeWithText("Settings").assertIsDisplayed()

    // Navigate back to Overview Screen
    composeTestRule.onNodeWithContentDescription("Navigate up").performClick()
    composeTestRule.onNodeWithText("Overview").assertIsDisplayed()

    // Navigate to Map Screen
    composeTestRule.onNodeWithText("Map").performClick()
    composeTestRule.onNodeWithText("Map").assertIsDisplayed()

    // Check if the location permission request is displayed (if applicable)
    // You may want to mock permissions or grant them in a real device scenario
    composeTestRule.onNodeWithText("Allow location access?").assertIsDisplayed()

    // Navigate back to Overview Screen
    composeTestRule.onNodeWithContentDescription("Navigate up").performClick()
    composeTestRule.onNodeWithText("Overview").assertIsDisplayed()

    // Navigate to Messages Screen
    composeTestRule.onNodeWithText("Messages").performClick()
    composeTestRule.onNodeWithText("Messages").assertIsDisplayed()

    // Navigate back to Overview Screen
    composeTestRule.onNodeWithContentDescription("Navigate up").performClick()
    composeTestRule.onNodeWithText("Overview").assertIsDisplayed()

    // Finally, you can test any other screens you have
    // For example, navigate to Contact Screen
    composeTestRule.onNodeWithText("Contacts").performClick()
    composeTestRule.onNodeWithText("Contacts").assertIsDisplayed()

    // And back again
    composeTestRule.onNodeWithContentDescription("Navigate up").performClick()
    composeTestRule.onNodeWithText("Overview").assertIsDisplayed()
  }

  private fun performSignIn(email: String, password: String) {
    // Simulate filling the email and password fields and clicking sign-in
    composeTestRule.onNodeWithTag("emailField").performTextInput(email)
    composeTestRule.onNodeWithTag("passwordField").performTextInput(password)
    composeTestRule.onNodeWithText("Sign In").performClick()
  }
}
