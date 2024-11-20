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
class StorageAndDataScreenTests {

  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)

    composeTestRule.setContent { StorageAndDataScreen(navigationActions = navigationActions) }
  }

  @Test
  fun displaysTopBarWithStorageAndDataTitle() {
    // Verify that the top bar title "Storage and Data" is displayed
    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
    composeTestRule.onNodeWithText("Storage and Data").assertIsDisplayed()
  }

  @Test
  fun goBackButtonCallsNavigationAction() {
    // Perform click on the back button and verify the goBack action is triggered
    composeTestRule.onNodeWithTag("backButton").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun displaysManageStorageSection() {
    // Verify that the "Manage storage" text and button are displayed
    composeTestRule.onNodeWithTag("manageStorageText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("manageStorageButton").assertIsDisplayed()
  }

  @Test
  fun displaysNetworkUsageSection() {
    // Verify that the "Network usage" text and "View network usage" button are displayed
    composeTestRule.onNodeWithTag("networkUsageText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("viewNetworkUsageButton").assertIsDisplayed()
  }

  @Test
  fun displaysMediaQualitySection() {
    // Verify that the "Media Quality" text and each media quality option are displayed
    composeTestRule.onNodeWithTag("mediaQualityText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mediaQualityOptions").assertIsDisplayed()
    composeTestRule.onNodeWithTag("StandardQualityOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("HDQualityOption").assertIsDisplayed()
  }
}
