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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

@RunWith(AndroidJUnit4::class)
class DiscussionScreenTests {

  private lateinit var navigationActions: NavigationActions

  @Mock private lateinit var userViewModel: UserViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this) // Initialize mock objects
    navigationActions = mock(NavigationActions::class.java, "navigationActions")

    composeTestRule.setContent {
      DiscussionScreen(navigationActions = navigationActions, userViewModel = userViewModel)
    }
  }

  @After
  fun tearDown() {
    // Fully reset the mock to avoid cross-test contamination
    clearInvocations(userViewModel)
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
  fun clickingDeleteAllChatsButtonDisplaysConfirmationDialog() {
    // Scroll to the "Delete all chats" section and perform click
    composeTestRule
        .onNodeWithTag("discussionLazyColumn")
        .performScrollToNode(hasTestTag("deleteAllChatsButton"))
    composeTestRule.onNodeWithTag("deleteAllChatsButton").performClick()

    // Verify that the confirmation dialog is displayed
    composeTestRule.onNodeWithText("Confirm Deletion").assertIsDisplayed()
    composeTestRule
        .onNodeWithText(
            "Are you sure you want to delete all messages? This action cannot be undone.")
        .assertIsDisplayed()
  }

  /*@Test
  fun clickingConfirmInDeleteDialogCallsDeleteAllMessages() = runBlocking {
    composeTestRule.waitForIdle()
    composeTestRule.waitForIdle()
    // Clear previous invocations to ensure the state is clean
    clearInvocations(userViewModel)
    // Scroll to the "Delete all chats" section and perform click
    composeTestRule
      .onNodeWithTag("discussionLazyColumn")
      .performScrollToNode(hasTestTag("deleteAllChatsButton"))
    composeTestRule.onNodeWithTag("deleteAllChatsButton").performClick()

    // Ensure UI is idle before clicking confirm
    composeTestRule.waitForIdle()

    // Click confirm button in the dialog
    composeTestRule.onNodeWithText("Confirm").performClick()

    // Ensure UI is idle after confirming
    composeTestRule.waitForIdle()

    // Verify that deleteAllMessages is called in UserViewModel
    verify(userViewModel, timeout(1000)).deleteAllMessages()
  }*/

  @Test
  fun clickingCancelInDeleteDialogDoesNotCallDeleteAllMessages() {
    composeTestRule.waitForIdle()
    // Clear previous invocations to ensure the state is clean
    clearInvocations(userViewModel)
    // Scroll to the "Delete all chats" section and perform click
    composeTestRule
        .onNodeWithTag("discussionLazyColumn")
        .performScrollToNode(hasTestTag("deleteAllChatsButton"))
    composeTestRule.onNodeWithTag("deleteAllChatsButton").performClick()

    // Ensure UI is idle before clicking cancel
    composeTestRule.waitForIdle()

    // Verify that the confirmation dialog is displayed
    composeTestRule.onNodeWithText("Confirm Deletion").assertIsDisplayed()

    // Click cancel button in the dialog
    composeTestRule.onNodeWithText("Cancel").performClick()

    // Ensure UI is idle after canceling
    composeTestRule.waitForIdle()

    // Verify that deleteAllMessages is NOT called in UserViewModel
    verifyNoInteractions(userViewModel)
  }
}
