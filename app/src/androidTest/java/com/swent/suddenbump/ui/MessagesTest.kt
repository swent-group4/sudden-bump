package com.swent.suddenbump.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.swent.suddenbump.ui.messages.MessagesScreen
import com.swent.suddenbump.ui.navigation.NavigationActions
import org.junit.Rule
import org.junit.Test

class MessagesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testBackButtonIsClickable() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val navigationActions = NavigationActions(navController)
            MessagesScreen(navigationActions)
        }

        // Vérifie que le bouton de retour est présent et cliquable
        composeTestRule.onNodeWithTag("back_button").assertHasClickAction()
    }

    @Test
    fun testMessagesListDisplayed() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val navigationActions = NavigationActions(navController)
            MessagesScreen(navigationActions)
        }

        // Vérifie que la liste des messages est affichée
        composeTestRule.onNodeWithTag("messages_list").assertExists()
    }

    @Test
    fun testMessageItemsDisplayed() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val navigationActions = NavigationActions(navController)
            MessagesScreen(navigationActions)
        }

        // Vérifie que chaque message est affiché avec le bon tag
        composeTestRule.onNodeWithTag("message_item_Gérard Barel").assertIsDisplayed()
        composeTestRule.onNodeWithTag("message_item_Clarisse Biquet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("message_item_Valérie Blouse").assertIsDisplayed()
        composeTestRule.onNodeWithTag("message_item_Marc, Julien").assertIsDisplayed()
    }

    @Test
    fun testDividersExist() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val navigationActions = NavigationActions(navController)
            MessagesScreen(navigationActions)
        }

        // Vérifie qu'il y a des diviseurs (lignes) entre les messages
        composeTestRule.onAllNodesWithTag("divider").assertCountEquals(3)  // Il devrait y avoir 3 lignes
    }

    @Test
    fun testMessageContentIsDisplayed() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val navigationActions = NavigationActions(navController)
            MessagesScreen(navigationActions)
        }

        // Fais défiler jusqu'au premier élément (ou l'élément souhaité)
        composeTestRule.onNodeWithTag("messages_list")
            .performScrollToIndex(0)

        // Vérifie que le message est affiché
        composeTestRule.onNodeWithText("Hi Marc! Thanks for your message")
            .assertIsDisplayed()
    }
}
