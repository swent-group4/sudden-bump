package com.swent.suddenbump.ui.authentication

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.suddenbump.MainActivity
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginTest : TestCase() {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @get:Rule val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

  @Before
  public fun setup() {
    Intents.init() // Initialize intents
  }

  @After
  public override fun tearDown() {
    Intents.release() // Release intents
  }

  @Test
  fun titleAndButtonAreCorrectlyDisplayed() {
    composeTestRule.waitUntil(timeoutMillis = 7000) {
      composeTestRule.onNodeWithTag("loginTitle").fetchSemanticsNode() != null
    }
    composeTestRule.onNodeWithTag("loginTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginTitle").assertTextEquals("SuddenBump!")

    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertHasClickAction()
  }

  @Test
  fun googleSignInReturnsValidActivityResult() {
    composeTestRule.waitUntil(timeoutMillis = 7000) {
      composeTestRule.onNodeWithTag("loginButton").fetchSemanticsNode() != null
    }
    composeTestRule.onNodeWithTag("loginButton").performClick()
    composeTestRule.waitForIdle()
    intended(toPackage("com.google.android.gms"))
  }
}
