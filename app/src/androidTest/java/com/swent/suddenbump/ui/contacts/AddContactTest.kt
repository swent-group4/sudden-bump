package com.swent.suddenbump.ui.contacts

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.swent.suddenbump.ui.contact.AddContactScreen
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify

class AddContactScreenTest {
  private lateinit var navigationActions: NavigationActions
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)

    `when`(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)
    composeTestRule.setContent { AddContactScreen(navigationActions) }
  }

  @Test
  fun testInitialScreenState() {
    // Verify the top bar title
    composeTestRule.onNodeWithText("Add contact").assertIsDisplayed()

    // Verify the search field is displayed
    composeTestRule.onNodeWithTag("searchTextField").assertIsDisplayed()

    // Verify the recommended row is displayed
    composeTestRule.onNodeWithTag("recommendedRow").assertIsDisplayed()

    // Verify the list of users is displayed
    composeTestRule.onNodeWithTag("userList").assertIsDisplayed()
  }

  @Test
  fun testSearchFunctionality() {

    // Enter a search query
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("John")

    // Verify that the list is filtered
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    composeTestRule.onNodeWithText("Jane Smith").assertDoesNotExist()
  }

  @Test
  fun testNoResultsMessage() {
    // Enter a search query that yields no results
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("NonExistentName")

    // Verify that the no results message is displayed
    composeTestRule
        .onNodeWithText("Looks like no user corresponds to your query")
        .assertIsDisplayed()
  }

  @Test
  fun testNavigationBackButton() {
    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("backButton").performClick()
    verify(navigationActions).goBack()
  }
}
