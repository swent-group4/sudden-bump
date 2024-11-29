package com.swent.suddenbump.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

class ConversationScreenTest {

  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Mock the navigation actions
    navigationActions = mock(NavigationActions::class.java)

    // Mock the current route to be "OVERVIEW"
    `when`(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)

    // Set the content for the Compose test rule
    composeTestRule.setContent { ConversationScreen(navigationActions) }
  }

  @Test
  fun hasRequiredComponents() {
    // Verify that the conversation screen container is displayed
    composeTestRule.onNodeWithTag("conversationScreen").assertIsDisplayed()

    // Verify that the bottom navigation menu is displayed
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }

  @Test
  fun conversationScreenTextIsDisplayed() {
    // Verify that the conversation text is displayed
    composeTestRule.onNodeWithTag("convText").assertIsDisplayed()
  }

  @Test
  fun bottomNavigationMenuNavigatesCorrectly() {

    // Simulate clicking on a tab in the bottom navigation menu
    val destinationRoute = Route.SETTINGS

    // Mock the navigation to the destination route
    doAnswer { `when`(navigationActions.currentRoute()).thenReturn(destinationRoute) }
        .`when`(navigationActions)
        .navigateTo(destinationRoute)

    // Perform a click on the corresponding tab (using a placeholder test tag)
    composeTestRule
        .onNodeWithTag("bottomNavigationMenu")
        .performClick() // Replace with actual tab selection logic if needed

    // Verify that the navigation action was triggered
    verify(navigationActions).navigateTo(destinationRoute)
  }
}
