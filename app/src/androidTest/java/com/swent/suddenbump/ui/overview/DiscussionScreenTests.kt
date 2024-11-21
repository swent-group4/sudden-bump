package com.swent.suddenbump.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
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
    userViewModel = mock(UserViewModel::class.java)

    composeTestRule.setContent {
      DiscussionScreen(navigationActions = navigationActions, userViewModel = userViewModel)
    }
  }

  @Test
  fun displaysTopBarWithChatsTitle() {
    // Verify that the top bar title "Chats" is displayed
    composeTestRule.onNodeWithText("Chats").assertIsDisplayed()
  }

  @Test
  fun goBackButtonCallsNavigationAction() {
    // Perform click on the back button and verify the goBack action is triggered
    composeTestRule.onNodeWithTag("backButton").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun displaysChatWallpaperSection() {
    // Scroll to the "Chat wallpaper" section before verifying
    composeTestRule
        .onNodeWithTag("discussionLazyColumn")
        .performScrollToNode(hasTestTag("chatWallpaperText"))
    composeTestRule.onNodeWithTag("chatWallpaperText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("changeChatWallpaperButton").assertIsDisplayed()
  }

  @Test
  fun displaysExportChatSection() {
    // Scroll to the "Export chat" section before verifying
    composeTestRule
        .onNodeWithTag("discussionLazyColumn")
        .performScrollToNode(hasTestTag("exportChatText"))
    composeTestRule.onNodeWithTag("exportChatText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("exportChatButton").assertIsDisplayed()
  }

  @Test
  fun displaysArchiveAllChatsSection() {
    // Scroll to the "Archive all chats" section before verifying
    composeTestRule
        .onNodeWithTag("discussionLazyColumn")
        .performScrollToNode(hasTestTag("archiveAllChatsText"))
    composeTestRule.onNodeWithTag("archiveAllChatsText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("archiveAllChatsButton").assertIsDisplayed()
  }

  @Test
  fun displaysClearAllChatsSection() {
    // Scroll to the "Clear all chats" section before verifying
    composeTestRule
        .onNodeWithTag("discussionLazyColumn")
        .performScrollToNode(hasTestTag("clearAllChatsText"))
    composeTestRule.onNodeWithTag("clearAllChatsText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("clearAllChatsButton").assertIsDisplayed()
  }

  @Test
  fun displaysDeleteAllChatsSection() {
    // Scroll to the "Delete all chats" section before verifying
    composeTestRule
        .onNodeWithTag("discussionLazyColumn")
        .performScrollToNode(hasTestTag("deleteAllChatsText"))
    composeTestRule.onNodeWithTag("deleteAllChatsText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteAllChatsButton").assertIsDisplayed()
  }

  @Test
  fun clickingDeleteAllChatsButtonShowsConfirmDialog() {
    // Scroll to the "Delete all chats" section and click the delete button
    composeTestRule
        .onNodeWithTag("discussionLazyColumn")
        .performScrollToNode(hasTestTag("deleteAllChatsButton"))
    composeTestRule.onNodeWithTag("deleteAllChatsButton").performClick()

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
  fun clickingConfirmButtonCallsDeleteAllMessages() {
    // Show the delete confirmation dialog
    composeTestRule
        .onNodeWithTag("discussionLazyColumn")
        .performScrollToNode(hasTestTag("deleteAllChatsButton"))
    composeTestRule.onNodeWithTag("deleteAllChatsButton").performClick()

    // Click on the confirm button
    composeTestRule.onNodeWithTag("confirmButton").performClick()

    // Verify that deleteAllMessages in userViewModel is called
    verify(userViewModel).deleteAllMessages()
  }

  @Test
  fun clickingCancelButtonDismissesDialog() {
    // Show the delete confirmation dialog
    composeTestRule
        .onNodeWithTag("discussionLazyColumn")
        .performScrollToNode(hasTestTag("deleteAllChatsButton"))
    composeTestRule.onNodeWithTag("deleteAllChatsButton").performClick()

    // Click on the cancel button
    composeTestRule.onNodeWithTag("cancelButton").performClick()

    // Verify that the confirm delete dialog is no longer displayed
    composeTestRule.onNodeWithText("Confirm Deletion").assertDoesNotExist()
  }

  @Test
  fun clickingChangeChatWallpaperButtonTriggersAction() {
    // Scroll to the "Change chat wallpaper" section and click the button
    composeTestRule
        .onNodeWithTag("discussionLazyColumn")
        .performScrollToNode(hasTestTag("changeChatWallpaperButton"))
    composeTestRule.onNodeWithTag("changeChatWallpaperButton").performClick()

    // Verify that the action related to changing chat wallpaper would be triggered (this might
    // involve verifying a method call, navigating, etc.)
    // Currently, there's no specific method to verify for wallpaper change, this is a placeholder
    // verify(navigationActions).navigateTo(Screen.CHANGE_WALLPAPER)
  }
}
