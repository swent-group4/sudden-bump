package com.swent.suddenbump.ui.overview

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.swent.suddenbump.ui.navigation.NavigationActions
import org.junit.Rule
import org.junit.Test

class FriendsListTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testInitialScreenState() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      FriendsListScreen(navigationActions)
    }

    // Verify the top bar title
    composeTestRule.onNodeWithText("Friends").assertIsDisplayed()

    // Verify the search field is displayed
    composeTestRule.onNodeWithTag("searchTextField").assertIsDisplayed()

    // Verify the list of users is displayed
    composeTestRule.onNodeWithTag("userList").assertIsDisplayed()
  }

  @Test
  fun testSearchFunctionality() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      FriendsListScreen(navigationActions)
    }

    // Enter a search query
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("John")

    // Verify that the list is filtered
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    composeTestRule.onNodeWithText("Jane Smith").assertDoesNotExist()
  }

  @Test
  fun testNoResultsMessage() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      FriendsListScreen(navigationActions)
    }

    // Enter a search query that yields no results
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("NonExistentName")

    // Verify that the no results message is displayed
    composeTestRule
        .onNodeWithText("Looks like no user corresponds to your query")
        .assertIsDisplayed()
  }

  @Test
  fun testNavigationActions() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      FriendsListScreen(navigationActions)
    }

    // Test back button navigation
    //        composeTestRule.onNodeWithTag("backButton").performClick()

    // Test add contact button navigation
    //        composeTestRule.onNodeWithTag("addContactButton").performClick()
  }
}
