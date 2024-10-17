package com.swent.suddenbump.ui.contacts

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.swent.suddenbump.ui.contact.AddContactScreen
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
    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("backButton").performClick()
    verify(navigationActions).goBack()
  }

      @Test
      fun testSendMessageButtonClick() {
          // Click the send message button
          composeTestRule.onNodeWithTag("sendMessageButton").assertIsDisplayed()
          composeTestRule.onNodeWithTag("sendMessageButton").performClick()
      }

  //    @Test
  //    fun testAddToContactsButtonWhenNotFriend() {
  //        composeTestRule.setContent {
  //            val navController = rememberNavController()
  //            val navigationActions = NavigationActions(navController)
  //            ContactScreen(navigationActions)
  //        }
  //
  //        // Modify the user to not be a friend
  //        composeTestRule.setContent {
  //            val navController = rememberNavController()
  //            val navigationActions = NavigationActions(navController)
  //            ContactScreen(navigationActions)
  //        }
  //
  //        // Verify the add to contacts button is displayed
  //        composeTestRule.onNodeWithTag("addToContactsButton").assertIsDisplayed()
  //
  //        // Click the add to contacts button
  ////        composeTestRule.onNodeWithTag("addToContactsButton").performClick()
  //    }
}
