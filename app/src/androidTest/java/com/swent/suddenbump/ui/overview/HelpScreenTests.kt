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
class HelpScreenTests {

  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)

    composeTestRule.setContent { HelpScreen(navigationActions = navigationActions) }
  }

  @Test
  fun displaysTopBarWithHelpTitle() {
    // Verify that the top bar title "Help" is displayed
    composeTestRule.onNodeWithText("Help").assertIsDisplayed()
  }

  @Test
  fun goBackButtonCallsNavigationAction() {
    // Perform click on the back button and verify the goBack action is triggered
    composeTestRule.onNodeWithTag("backButton").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun displaysHelpCenterSection() {
    // Verify that the "Help Center" text and "Visit Help Center" button are displayed
    composeTestRule.onNodeWithTag("helpCenterText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("visitHelpCenterButton").assertIsDisplayed()
  }

  @Test
  fun displaysContactUsSection() {
    // Verify that the "Contact Us" text and "Get in Touch" button are displayed
    composeTestRule.onNodeWithTag("contactUsText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("contactUsButton").assertIsDisplayed()
  }

  @Test
  fun displaysTermsAndPrivacyPolicySection() {
    // Verify that the "Terms and Privacy Policy" text and "View Terms and Privacy Policy" button
    // are displayed
    composeTestRule.onNodeWithTag("termsPrivacyPolicyText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("viewTermsPrivacyPolicyButton").assertIsDisplayed()
  }

  @Test
  fun displaysLicensesSection() {
    // Verify that the "Licenses" text and "View Licenses" button are displayed
    composeTestRule.onNodeWithTag("licensesText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("viewLicensesButton").assertIsDisplayed()
  }

  @Test
  fun displaysFooter() {
    // Verify that the footer text is displayed
    composeTestRule.onNodeWithTag("footerText").assertIsDisplayed()
  }
}
