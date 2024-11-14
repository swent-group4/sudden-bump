/*package com.swent.suddenbump.ui.settings

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.overview.SettingsScreen
import com.swent.suddenbump.model.user.UserViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val navigationActions = NavigationActions(navController)
            val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
            SettingsScreen(navigationActions, userViewModel)
        }
    }

    @Test
    fun testProfilePictureIsDisplayed() {
        composeTestRule.onNodeWithTag("profilePicture").assertExists()
    }

    @Test
    fun testAddPhotoButtonIsClickable() {
        composeTestRule.onNodeWithTag("addPhotoButton")
            .assertExists()
            .assertIsEnabled()
            .performClick()
    }

    @Test
    fun testUsernameFieldUpdatesCorrectly() {
        val newUsername = "NewUser123"
        composeTestRule.onNodeWithTag("usernameField")
            .assertExists()
            .performTextInput(newUsername)
            .assertTextEquals(newUsername)
    }

    @Test
    fun testNotificationsButtonIsClickable() {
        composeTestRule.onNodeWithText("Notifications")
            .assertExists()
            .assertIsEnabled()
            .performClick()
    }

    @Test
    fun testAccountSectionIsClickable() {
        composeTestRule.onNodeWithText("Account")
            .assertExists()
            .assertIsEnabled()
            .performClick()
    }

    @Test
    fun testConfidentialitySectionIsClickable() {
        composeTestRule.onNodeWithText("Confidentiality")
            .assertExists()
            .assertIsEnabled()
            .performClick()
    }

    @Test
    fun testDiscussionsSectionIsClickable() {
        composeTestRule.onNodeWithText("Discussions")
            .assertExists()
            .assertIsEnabled()
            .performClick()
    }

    @Test
    fun testStorageAndDataSectionIsClickable() {
        composeTestRule.onNodeWithText("Storage and Data")
            .assertExists()
            .assertIsEnabled()
            .performClick()
    }

    @Test
    fun testHelpButtonIsClickable() {
        composeTestRule.onNodeWithText("Help")
            .assertExists()
            .assertIsEnabled()
            .performClick()
    }

    @Test
    fun testLocationStatusSwitchToggles() {
        val locationSwitch = composeTestRule.onNodeWithText("Location Sharing")
        locationSwitch.assertExists()
        locationSwitch.assertIsToggleable()
        locationSwitch.performClick().assertIsOff()
        locationSwitch.performClick().assertIsOn()
    }

    @Test
    fun testGoBackButtonIsClickable() {
        composeTestRule.onNodeWithTag("goBackButton")
            .assertExists()
            .assertIsEnabled()
            .performClick()
    }

    @Test
    fun testLanguageDropdownSelectionChanges() {
        composeTestRule.onNodeWithTag("languageButton")
            .assertExists()
            .performClick()

        composeTestRule.onNodeWithText("French")
            .assertExists()
            .performClick()

        composeTestRule.onNodeWithTag("languageButton")
            .assertTextContains("French")
    }
}*/
