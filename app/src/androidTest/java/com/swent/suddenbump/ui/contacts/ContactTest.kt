package com.swent.suddenbump.ui.contacts

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.swent.suddenbump.ui.contact.ContactScreen
import com.swent.suddenbump.ui.navigation.NavigationActions
import org.junit.Rule
import org.junit.Test

class ContactScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testInitialScreenState() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val navigationActions = NavigationActions(navController)
            ContactScreen(navigationActions)
        }

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

//    @Test
//    fun testSendMessageButtonClick() {
//        composeTestRule.setContent {
//            val navController = rememberNavController()
//            val navigationActions = NavigationActions(navController)
//            ContactScreen(navigationActions)
//        }
//
//        // Click the send message button
//        composeTestRule.onNodeWithTag("sendMessageButton").assertIsDisplayed()
//
//    }

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