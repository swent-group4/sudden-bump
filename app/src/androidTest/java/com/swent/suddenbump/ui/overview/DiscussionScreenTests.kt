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

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    val userViewModel = mock(UserViewModel::class.java) // Add this line

    composeTestRule.setContent {
      DiscussionScreen(
          navigationActions = navigationActions, userViewModel = userViewModel // Add this line
          )
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
}
