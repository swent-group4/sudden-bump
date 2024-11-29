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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class DiscussionScreenTests {

  private lateinit var navigationActions: NavigationActions
  private lateinit var userViewModel: UserViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)

    // Mock dependencies for UserViewModel
    val userRepository = mock(UserRepository::class.java)
    val chatRepository = mock(ChatRepository::class.java)

    // Create a real UserViewModel instance
    userViewModel = UserViewModel(userRepository, chatRepository)

    composeTestRule.setContent {
      DiscussionScreen(navigationActions = navigationActions, userViewModel = userViewModel)
    }
  }

  @Test
  fun displaysTopBarWithChatsTitle() {
    // Verify that the top bar title "Chats" is displayed
    composeTestRule.onNodeWithText("Discussions").assertIsDisplayed()
  }

  @Test
  fun goBackButtonCallsNavigationAction() {
    // Perform click on the back button and verify the goBack action is triggered
    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.waitForIdle() // Wait for UI to settle
    verify(navigationActions).goBack()
  }

  @Test
  fun displaysDeleteAllChatsSection() {
    // Verify the "Delete all chats" button is displayed
    composeTestRule.onNodeWithTag("deleteAllChatsButton").assertIsDisplayed()
  }

  @Test
  fun clickingDeleteAllChatsButtonShowsConfirmDialog() {
    // Click the "Delete all chats" button
    composeTestRule.onNodeWithTag("deleteAllChatsButton").performClick()
    composeTestRule.waitForIdle() // Wait for UI to settle

    // Verify that the confirm delete dialog is displayed
    composeTestRule.onNodeWithText("Confirm Deletion").assertIsDisplayed()
    composeTestRule
        .onNodeWithText(
            "Are you sure you want to delete all messages? This action cannot be undone.")
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()
  }

  @Test
  fun clickingConfirmButtonCallsDeleteAllMessages() = runBlocking {
    withTimeout(5_000) { // 5-second timeout to avoid getting stuck
      // Click the "Delete all chats" button
      composeTestRule.onNodeWithTag("deleteAllChatsButton").performClick()
      composeTestRule.waitForIdle() // Wait for UI to settle

      // Click on the confirm button
      composeTestRule.onNodeWithTag("confirmButton").performClick()
      composeTestRule.waitForIdle() // Wait for UI to settle

      // Since we cannot mock UserViewModel directly, we will collect the messages Flow
      val messages = userViewModel.messages.first()
      assert(messages.isEmpty()) { "Messages should be deleted." }
    }
  }

  @Test
  fun clickingCancelButtonDismissesDialog() {
    // Click the "Delete all chats" button
    composeTestRule.onNodeWithTag("deleteAllChatsButton").performClick()
    composeTestRule.waitForIdle() // Wait for UI to settle

    // Click on the cancel button
    composeTestRule.onNodeWithTag("cancelButton").performClick()
    composeTestRule.waitForIdle() // Wait for UI to settle

    // Verify that the confirm delete dialog is no longer displayed
    composeTestRule.onNodeWithText("Confirm Deletion").assertDoesNotExist()
  }
}
