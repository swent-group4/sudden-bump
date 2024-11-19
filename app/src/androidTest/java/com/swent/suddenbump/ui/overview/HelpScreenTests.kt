package com.swent.suddenbump.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
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
    composeTestRule.onNodeWithTag("helpTitle").assertIsDisplayed()
  }

  @Test
  fun goBackButtonCallsNavigationAction() {
    // Perform click on the back button and verify the goBack action is triggered
    composeTestRule.onNodeWithTag("backButton").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun displaysHelpCenterSection() {
    // Scroll to the "Help Center" section before verifying
    composeTestRule
        .onNodeWithTag("helpScreenScrollable")
        .performScrollToNode(hasTestTag("helpCenterText"))
    composeTestRule.onNodeWithTag("helpCenterText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("visitHelpCenterButton").assertIsDisplayed()
  }

  @Test
  fun displaysContactUsSection() {
    // Scroll to the "Contact Us" section before verifying
    composeTestRule
        .onNodeWithTag("helpScreenScrollable")
        .performScrollToNode(hasTestTag("contactUsText"))
    composeTestRule.onNodeWithTag("contactUsText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("contactUsButton").assertIsDisplayed()
  }

  @Test
  fun displaysTermsAndPrivacyPolicySection() {
    // Scroll to the "Terms and Privacy Policy" section before verifying
    composeTestRule
        .onNodeWithTag("helpScreenScrollable")
        .performScrollToNode(hasTestTag("termsPrivacyPolicyText"))
    composeTestRule.onNodeWithTag("termsPrivacyPolicyText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("viewTermsPrivacyPolicyButton").assertIsDisplayed()
  }

  @Test
  fun displaysLicensesSection() {
    // Scroll to the "Licenses" section before verifying
    composeTestRule
        .onNodeWithTag("helpScreenScrollable")
        .performScrollToNode(hasTestTag("licensesText"))
    composeTestRule.onNodeWithTag("licensesText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("viewLicensesButton").assertIsDisplayed()
  }

  @Test
  fun displaysFooter() {
    // Scroll to the footer and verify it is displayed
    composeTestRule
        .onNodeWithTag("helpScreenScrollable")
        .performScrollToNode(hasTestTag("footerText"))
    composeTestRule.onNodeWithTag("footerText").assertIsDisplayed()
  }
}
