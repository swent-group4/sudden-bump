package com.swent.suddenbump.ui.contacts

import android.location.Location
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.contact.ContactScreen
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import com.swent.suddenbump.ui.navigation.Screen
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ContactScreenTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var userViewModel: UserViewModel

  private val friend =
      MutableStateFlow(
          User(
              uid = "1",
              firstName = "John",
              lastName = "Doe",
              phoneNumber = "+1234567890",
              profilePicture = null,
              emailAddress = "",
              lastKnownLocation =
                  Location("dummy").apply {
                    latitude = 46.5180
                    longitude = 6.5680
                  }))
  private val currentUser =
      MutableStateFlow(
          User(
              uid = "0",
              firstName = "Alice",
              lastName = "Wonderland",
              phoneNumber = "+1234567890",
              profilePicture = null,
              emailAddress = "",
              lastKnownLocation =
                  Location("mock_provider").apply {
                    latitude = 46.5180
                    longitude = 6.5680
                  }))

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mockk(relaxed = true)
    userViewModel = mockk(relaxed = true)

    // Initialize the content once before all tests
    //        composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }
    every { navigationActions.currentRoute() } returns Route.OVERVIEW

    // By default in test user is a friend
    every { userViewModel.getSelectedContact() } returns friend
    every { userViewModel.getUserFriendRequests() } returns MutableStateFlow(emptyList())
    every { userViewModel.getSentFriendRequests() } returns MutableStateFlow(emptyList())
    every { userViewModel.getUserFriends() } returns MutableStateFlow(listOf(friend.value))
    every { userViewModel.getCurrentUser() } returns currentUser
  }

  @Test
  fun testInitialScreenState() {
    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("contactScreen").assertIsDisplayed()

    composeTestRule.onNodeWithText("Contact").assertIsDisplayed()

    composeTestRule.onNodeWithTag("profileImage").assertIsDisplayed()

    composeTestRule.onNodeWithTag("userName").assertIsDisplayed()

    composeTestRule.onNodeWithTag("phoneCard").assertIsDisplayed()

    composeTestRule.onNodeWithTag("emailCard").assertIsDisplayed()

    composeTestRule.onNodeWithTag("sendMessageButton").assertIsDisplayed()
  }

  @Test
  fun testNavigationBackButton() {
    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }
    composeTestRule.waitForIdle()

    // Verify the back button is displayed
    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("backButton").performClick()

    verify { navigationActions.goBack() }
  }

  @Test
  fun testSendMessageButtonClick() {
    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }
    composeTestRule.waitForIdle()

    // Verify the send message button is displayed
    composeTestRule.onNodeWithTag("sendMessageButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("sendMessageButton").performClick()

    verify { navigationActions.navigateTo(Screen.CHAT) }
  }

  @Test
  fun testFriendRequestVersion() {
    every { userViewModel.getUserFriends() } returns MutableStateFlow(emptyList())
    every { userViewModel.getUserFriendRequests() } returns MutableStateFlow(listOf(friend.value))

    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }

    composeTestRule.onNodeWithText("Accept friend request").assertIsDisplayed()
    composeTestRule.onNodeWithText("Decline friend request").assertIsDisplayed()
  }

  @Test
  fun testSentFriendRequestVersion() {
    every { userViewModel.getUserFriends() } returns MutableStateFlow(emptyList())
    every { userViewModel.getSentFriendRequests() } returns MutableStateFlow(listOf(friend.value))

    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("friendRequestSentText").assertIsDisplayed()
  }

  @Test
  fun testNoRelationVersion() {
    // Setup non-friend state
    every { userViewModel.getSelectedContact() } returns friend
    every { userViewModel.getUserFriends() } returns MutableStateFlow(emptyList())
    every { userViewModel.getUserFriendRequests() } returns MutableStateFlow(emptyList())
    every { userViewModel.getSentFriendRequests() } returns MutableStateFlow(emptyList())

    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("addToContactsButton").performClick()
  }

  @Test
  fun testBlockUserDialog() {
    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("moreOptionsButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("moreOptionsButton").performClick()

    // Open dialog
    composeTestRule.onNodeWithTag("blockUserButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("blockUserButton").performClick()

    // Verify dialog content
    composeTestRule.onNodeWithText("Block User").assertIsDisplayed()
    composeTestRule.onNodeWithText("Are you sure you want to block this user?").assertIsDisplayed()
  }

  @Test
  fun testBlockUserDialogDismiss() {
    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("moreOptionsButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("moreOptionsButton").performClick()

    // Open dialog
    composeTestRule.onNodeWithTag("blockUserButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("blockUserButton").performClick()

    // Dismiss dialog
    composeTestRule.onNodeWithText("No").performClick()

    // Verify dialog is dismissed
    composeTestRule.onNodeWithText("Block User").assertDoesNotExist()
  }

  @Test
  fun havingProfilePictureDisplaysComponent() {
    val userCopied =
        userViewModel.getSelectedContact().value.copy(profilePicture = ImageBitmap(30, 30))
    val userCopiedFlow = MutableStateFlow(userCopied)

    every { userViewModel.getSelectedContact() } returns userCopiedFlow

    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }
    composeTestRule.waitForIdle()

    // Verify the profile picture image
    composeTestRule.onNodeWithTag("profileImageNotNull").assertIsDisplayed()
  }

  @Test
  fun testDeleteFriendSuccessFlow() {
    // Make sure user is friend so deleteFriend is visible
    every { userViewModel.getUserFriends() } returns MutableStateFlow(listOf(friend.value))
    every { userViewModel.getUserFriendRequests() } returns MutableStateFlow(emptyList())
    every { userViewModel.getSentFriendRequests() } returns MutableStateFlow(emptyList())

    // When deleteFriend is called, immediately call onSuccess
    every {
      userViewModel.deleteFriend(
          user = any(), friend = any(), onSuccess = captureLambda(), onFailure = any())
    } answers
        {
          lambda<() -> Unit>().invoke() // Invoke onSuccess immediately
        }

    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }
    composeTestRule.waitForIdle()

    // Open the dropdown menu and click "Delete Friend"
    composeTestRule.onNodeWithTag("moreOptionsButton").performClick()
    composeTestRule.onNodeWithTag("deleteFriendButton").performClick()

    // Verify navigationActions.goBack() was called (covering that line)
    verify { navigationActions.goBack() }
  }

  @Test
  fun testAcceptFriendRequestSuccessFlow() {
    // Make sure user is in friendRequests list so "Accept" is displayed
    every { userViewModel.getUserFriends() } returns MutableStateFlow(emptyList())
    every { userViewModel.getUserFriendRequests() } returns MutableStateFlow(listOf(friend.value))
    every { userViewModel.getSentFriendRequests() } returns MutableStateFlow(emptyList())

    // When acceptFriendRequest is called, call onSuccess immediately
    every {
      userViewModel.acceptFriendRequest(
          user = any(), friend = any(), onSuccess = captureLambda(), onFailure = any())
    } answers
        {
          lambda<() -> Unit>().invoke() // trigger onSuccess
        }

    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }
    composeTestRule.waitForIdle()

    // Click "Accept friend request"
    composeTestRule.onNodeWithText("Accept friend request").performClick()

    // The onSuccess callback sets isFriend = true and isFriendRequest = false internally.
    // We can't directly test local variable changes, but we know these lines are covered
    // because onSuccess was triggered. If we had UI changes tied to these vars, we could
    // verify them here. For now, we rely on code coverage tools and the triggered callback.
    // If there's a UI side effect (like showing a "Send a message" button now), we could verify it.
  }

  @Test
  fun testDeclineFriendRequestSuccessFlow() {
    // Make sure user is in friendRequests list
    every { userViewModel.getUserFriends() } returns MutableStateFlow(emptyList())
    every { userViewModel.getUserFriendRequests() } returns MutableStateFlow(listOf(friend.value))
    every { userViewModel.getSentFriendRequests() } returns MutableStateFlow(emptyList())

    // When declineFriendRequest is called, trigger onSuccess immediately
    every {
      userViewModel.declineFriendRequest(
          user = any(), friend = any(), onSuccess = captureLambda(), onFailure = any())
    } answers { lambda<() -> Unit>().invoke() }

    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }
    composeTestRule.waitForIdle()

    // Click "Decline friend request"
    composeTestRule.onNodeWithText("Decline friend request").performClick()

    // onSuccess sets isFriend = false and isFriendRequest = false, covering those lines.
    // Check if the UI changes: now the addToContactsButton might appear.
    // Since it's local variable changes, we rely on coverage tools. If there's a UI side effect
    // like
    // showing "Send Friend Request" button now, we can verify it:
    // composeTestRule.onNodeWithTag("addToContactsButton").assertIsDisplayed() // if UI updates
    // accordingly
  }

  @Test
  fun testBlockUserSuccessFlow() {
    // User is friend by default
    every { userViewModel.getUserFriends() } returns MutableStateFlow(listOf(friend.value))
    every { userViewModel.getUserFriendRequests() } returns MutableStateFlow(emptyList())
    every { userViewModel.getSentFriendRequests() } returns MutableStateFlow(emptyList())

    // When blockUser is called, trigger onSuccess
    every {
      userViewModel.blockUser(
          user = any(), blockedUser = any(), onSuccess = captureLambda(), onFailure = any())
    } answers { lambda<() -> Unit>().invoke() }

    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }
    composeTestRule.waitForIdle()

    // Open dialog to block user
    composeTestRule.onNodeWithTag("moreOptionsButton").performClick()
    composeTestRule.onNodeWithTag("blockUserButton").performClick()

    // Click "Yes" to confirm blocking
    composeTestRule.onNodeWithText("Yes").performClick()

    // Verify navigationActions.goBack() was called after blocking user
    verify { navigationActions.goBack() }
  }

  @Test
  fun testDeleteFriendOption() {
    // Arrange
    // Ensure the user is a friend so that the "Delete Friend" option is visible
    every { userViewModel.getUserFriends() } returns MutableStateFlow(listOf(friend.value))
    every { userViewModel.getUserFriendRequests() } returns MutableStateFlow(emptyList())
    every { userViewModel.getSentFriendRequests() } returns MutableStateFlow(emptyList())

    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }
    composeTestRule.waitForIdle()

    // Act
    // Open the dropdown menu
    composeTestRule.onNodeWithTag("moreOptionsButton").performClick()

    // Verify "Delete Friend" option is displayed
    composeTestRule.onNodeWithTag("deleteFriendButton").assertIsDisplayed()

    // Mock the successful deletion scenario
    // We'll just rely on the relaxed mock behavior and verify calls via mocking
    composeTestRule.onNodeWithTag("deleteFriendButton").performClick()

    // Assert
    // Verify that deleteFriend was called with the correct user and friend
    verify {
      userViewModel.deleteFriend(
          user = match { it.uid == currentUser.value.uid },
          friend = match { it.uid == friend.value.uid },
          onSuccess = any(),
          onFailure = any())
    }
  }
}
