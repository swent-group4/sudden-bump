package com.swent.suddenbump.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
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

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)

    composeTestRule.setContent { DiscussionScreen(navigationActions = navigationActions) }
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
    // Verify that the "Chat wallpaper" text and "Change chat wallpaper" button are displayed
    composeTestRule.onNodeWithTag("chatWallpaperText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("changeChatWallpaperButton").assertIsDisplayed()
  }

  @Test
  fun displaysExportChatSection() {
    // Verify that the "Export chat" text and "Export chat" button are displayed
    composeTestRule.onNodeWithTag("exportChatText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("exportChatButton").assertIsDisplayed()
  }

  @Test
  fun displaysArchiveAllChatsSection() {
    // Verify that the "Archive all chats" text and "Archive all chats" button are displayed
    composeTestRule.onNodeWithTag("archiveAllChatsText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("archiveAllChatsButton").assertIsDisplayed()
  }

  @Test
  fun displaysClearAllChatsSection() {
    // Verify that the "Clear all chats" text and "Clear all chats" button are displayed
    composeTestRule.onNodeWithTag("clearAllChatsText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("clearAllChatsButton").assertIsDisplayed()
  }

  @Test
  fun displaysDeleteAllChatsSection() {
    // Verify that the "Delete all chats" text and "Delete all chats" button are displayed
    composeTestRule.onNodeWithTag("deleteAllChatsText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteAllChatsButton").assertIsDisplayed()
  }
}
