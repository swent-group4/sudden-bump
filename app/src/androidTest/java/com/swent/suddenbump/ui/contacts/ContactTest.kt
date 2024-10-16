package com.swent.suddenbump.ui.contacts

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.swent.suddenbump.ui.contact.ContactScreen
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify

class ContactScreenTest {
  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    `when`(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)

    // Initialize the content once before all tests
    composeTestRule.setContent { ContactScreen(navigationActions) }
  }

  @Test
  fun testInitialScreenState() {
    // Verify the top bar title
    composeTestRule.onNodeWithText("Contact").assertIsDisplayed()

    // Verify the profile image is displayed
    composeTestRule.onNodeWithTag("profileImage").assertIsDisplayed()

    // Verify the user's name is displayed
    composeTestRule.onNodeWithTag("userName").assertIsDisplayed()

    // Verify the birthday card is displayed
    composeTestRule.onNodeWithTag("birthdayCard").assertIsDisplayed()

    // Verify the phone card is displayed
    composeTestRule.onNodeWithTag("phoneCard").assertIsDisplayed()

    // Verify the email card is displayed
    composeTestRule.onNodeWithTag("emailCard").assertIsDisplayed()

    // Verify the send message button is displayed
    composeTestRule.onNodeWithTag("sendMessageButton").assertIsDisplayed()
  }

  @Test
  fun testNavigationBackButton() {
    // Verify the back button is displayed
    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()

    // Perform click on the back button
    composeTestRule.onNodeWithTag("backButton").performClick()

    // Verify that navigation action 'goBack' is called
    verify(navigationActions).goBack()
  }

  @Test
  fun testSendMessageButtonClick() {
    // Verify the send message button is displayed
    composeTestRule.onNodeWithTag("sendMessageButton").assertIsDisplayed()

    // Perform click on the send message button
    composeTestRule.onNodeWithTag("sendMessageButton").performClick()

    // Verify that navigation to the message screen is triggered
    //    verify(navigationActions).navigateTo(Route.MESSAGE)
  }

  @Test
  fun testBirthdayCardDisplaysCorrectInfo() {
    // Verify the birthday card is displayed
    composeTestRule.onNodeWithTag("birthdayCard").assertIsDisplayed()

    // Verify the birthday text is correct
    composeTestRule.onNodeWithTag("birthdayText").assertTextEquals("Birthday: 28 February 1998")
  }

  @Test
  fun testContactNameDisplaysCorrectly() {
    composeTestRule.onNodeWithTag("userName").assertTextEquals("Jane Smith")
  }
}
