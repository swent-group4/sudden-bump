package com.swent.suddenbump.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
  private var notificationsEnabled = true

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
          onNotificationsEnabledChange = { notificationsEnabled = it })
    }
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settingsTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addPhotoButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
  }

  @Test
  fun goBackButtonCallsNavActions() {
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").performClick()
    composeTestRule.waitForIdle() // Wait for UI to settle after the click
    verify(navigationActions).goBack()
  }

  @Test
  fun accountButtonNavigatesToAccountScreen() {
    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Account").performClick()
    composeTestRule.waitForIdle() // Wait for UI to settle after the click
    verify(navigationActions).navigateTo(screen = Screen.ACCOUNT)
  }

  @Test
  fun confidentialityButtonNavigatesToConfidentialityScreen() {
    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Confidentiality").performClick()
    composeTestRule.waitForIdle() // Wait for UI to settle after the click
    verify(navigationActions).navigateTo(screen = Screen.CONFIDENTIALITY)
  }

  @Test
  fun discussionsButtonNavigatesToDiscussionsScreen() {
    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Discussions").performClick()
    composeTestRule.waitForIdle() // Wait for UI to settle after the click
    verify(navigationActions).navigateTo(screen = Screen.DISCUSSIONS)
  }

  object Screen {
    const val ACCOUNT = "AccountScreen"
    const val CONFIDENTIALITY = "ConfidentialityScreen"
    const val DISCUSSIONS = "DiscussionsScreen"
  }
}
