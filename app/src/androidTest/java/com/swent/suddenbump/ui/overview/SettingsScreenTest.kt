package com.swent.suddenbump.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

  private lateinit var navigationActions: NavigationActions
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var chatRepository: ChatRepository

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    userRepository = mock(UserRepository::class.java)
    chatRepository = mock(ChatRepository::class.java)
    userViewModel = UserViewModel(userRepository, chatRepository)

    composeTestRule.setContent {
      SettingsScreen(
          navigationActions = navigationActions,
          userViewModel = userViewModel,
          onNotificationsEnabledChange = {})
    }
  }

  @Test
  fun hasRequiredComponents() {
    // Verify that the settings screen container is displayed
    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()

    // Verify that the top bar title is displayed
    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()

    // Verify other required components
    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addPhotoButton").assertIsDisplayed()
  }

  @Test
  fun goBackButtonCallsNavActions() {
    // Verify that the back button is displayed
    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()

    // Perform click on the back button
    composeTestRule.onNodeWithTag("backButton").performClick()

    // Verify the goBack action is triggered
    verify(navigationActions).goBack()
  }

  @Test
  fun accountButtonNavigatesToAccountScreen() {
    // Verify that the Account option navigates to the Account screen
    composeTestRule.onNodeWithTag("AccountOption").performClick()
    verify(navigationActions).navigateTo("AccountScreen")
  }

  @Test
  fun confidentialityButtonNavigatesToConfidentialityScreen() {
    // Verify that the Confidentiality option navigates to the Confidentiality screen
    composeTestRule.onNodeWithTag("ConfidentialityOption").performClick()
    verify(navigationActions).navigateTo("ConfidentialityScreen")
  }

  @Test
  fun discussionsButtonNavigatesToDiscussionsScreen() {
    // Verify that the Discussions option navigates to the Discussions screen
    composeTestRule.onNodeWithTag("DiscussionsOption").performClick()
    verify(navigationActions).navigateTo("DiscussionsScreen")
  }
}
