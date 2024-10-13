package com.swent.suddenbump.screen
//package com.swent.suddenbump.ui.settings

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.swent.suddenbump.ui.navigation.NavigationActions

import com.swent.suddenbump.ui.settings.SettingsScreen
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreenDisplaysCorrectly() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val navigationActions = NavigationActions(navController)
            SettingsScreen(navigationActions)
        }

        // Check that the settings title is displayed
        composeTestRule.onNodeWithTag("settingsTitle").assertExists().assertTextContains("Settings")

        // Check that the profile picture is displayed
        composeTestRule.onNodeWithTag("profilePicture").assertExists()

        // Check that the username field is displayed
        composeTestRule.onNodeWithTag("usernameField").assertExists()

        // Check that the notifications switch is displayed
        composeTestRule.onNodeWithTag("notificationsSwitch").assertExists()

        // Check that the dark mode switch is displayed
        composeTestRule.onNodeWithTag("darkModeSwitch").assertExists()

        // Check that the visibility button is displayed
        composeTestRule.onNodeWithTag("visibilityButton").assertExists()
    }

    @Test
    fun usernameCanBeUpdated() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val navigationActions = NavigationActions(navController)
            SettingsScreen(navigationActions)
        }

        // Check that the initial username is "User123"
        composeTestRule.onNodeWithTag("usernameField").assertTextEquals("User123")

        // Change the username to a new value
        composeTestRule.onNodeWithTag("usernameField").performTextInput("NewUser456")

        // Assert that the username has changed
        composeTestRule.onNodeWithTag("usernameField").assertTextEquals("NewUser456")
    }

    @Test
    fun notificationsSwitchTogglesCorrectly() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val navigationActions = NavigationActions(navController)
            SettingsScreen(navigationActions)
        }

        // Check that the notifications switch is initially enabled
        composeTestRule.onNodeWithTag("notificationsSwitch").assertIsOn()

        // Toggle the switch off
        composeTestRule.onNodeWithTag("notificationsSwitch").performClick()

        // Check that the switch is now off
        composeTestRule.onNodeWithTag("notificationsSwitch").assertIsOff()
    }

    @Test
    fun darkModeSwitchTogglesCorrectly() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val navigationActions = NavigationActions(navController)
            SettingsScreen(navigationActions)
        }

        // Check that the dark mode switch is initially off
        composeTestRule.onNodeWithTag("darkModeSwitch").assertIsOff()

        // Toggle the switch on
        composeTestRule.onNodeWithTag("darkModeSwitch").performClick()

        // Check that the switch is now on
        composeTestRule.onNodeWithTag("darkModeSwitch").assertIsOn()
    }

    @Test
    fun visibilityButtonOpensDropdownMenu() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val navigationActions = NavigationActions(navController)
            SettingsScreen(navigationActions)
        }

        // Check the default visibility value
        composeTestRule.onNodeWithTag("visibilityButton").assertTextContains("Visible for all")

        // Click the visibility button to expand the dropdown
        composeTestRule.onNodeWithTag("visibilityButton").performClick()

        // Check if the dropdown menu items are displayed
        composeTestRule.onNodeWithText("Visible for all").assertExists()
        composeTestRule.onNodeWithText("Visible for my contacts").assertExists()
        composeTestRule.onNodeWithText("Visible for my friends").assertExists()

        // Select "Visible for my friends"
        composeTestRule.onNodeWithText("Visible for my friends").performClick()

        // Verify that the visibility button now reflects the new selection
        composeTestRule.onNodeWithTag("visibilityButton").assertTextContains("Visible for my friends")
    }

    @Test
    fun goBackButtonNavigatesBack() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val navigationActions = NavigationActions(navController)
            SettingsScreen(navigationActions)
        }

        // Simulate clicking the go back button
        composeTestRule.onNodeWithTag("goBackButton").performClick()


    }
}