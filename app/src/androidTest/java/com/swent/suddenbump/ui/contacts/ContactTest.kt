package com.swent.suddenbump.ui.contacts

import android.location.Location
import androidx.compose.ui.graphics.ImageBitmap
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

    composeTestRule.onNodeWithTag("contactScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Contact").assertIsDisplayed()

    composeTestRule.onNodeWithTag("profileImage_1").assertIsDisplayed()

    composeTestRule.onNodeWithTag("profileImage_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("userName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("phoneCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emailCard").assertIsDisplayed()

    // Since user is friend by default, "Send a message" should show
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

    composeTestRule.onNodeWithTag("sendMessageButton").assertIsDisplayed().performClick()

    verify { navigationActions.navigateTo(Screen.CHAT) }
  }

  @Test
  fun testUnsendFriendRequest() {
    // Arrange
    // Set scenario: user is in sentFriendRequests
    every { userViewModel.getUserFriends() } returns MutableStateFlow(emptyList())
    every { userViewModel.getUserFriendRequests() } returns MutableStateFlow(emptyList())
    every { userViewModel.getSentFriendRequests() } returns MutableStateFlow(listOf(friend.value))

    // Mock unsendFriendRequest to immediately call onSuccess
    every {
      userViewModel.unsendFriendRequest(
          user = any(), friend = any(), onSuccess = captureLambda(), onFailure = any())
    } answers
        {
          lambda<() -> Unit>().invoke() // call onSuccess immediately
        }

    // Act
    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }

    // The "Requested" button should be displayed since it's a sent friend request scenario
    composeTestRule.onNodeWithTag("unsendFriendRequestButton").assertIsDisplayed()

    // Perform click on the "Requested" button
    composeTestRule.onNodeWithTag("unsendFriendRequestButton").performClick()

    // Assert
    // Verify that unsendFriendRequest was called with the correct arguments
    verify {
      userViewModel.unsendFriendRequest(
          user = match { it.uid == currentUser.value.uid },
          friend = match { it.uid == friend.value.uid },
          onSuccess = any(),
          onFailure = any())
    }
    // For now, we've verified that the action is triggered correctly.
  }

  @Test
  fun testFriendRequestVersion() {
    // User is not a friend but in friend requests
    every { userViewModel.getUserFriends() } returns MutableStateFlow(emptyList())
    every { userViewModel.getUserFriendRequests() } returns MutableStateFlow(listOf(friend.value))
    every { userViewModel.getSentFriendRequests() } returns MutableStateFlow(emptyList())

    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }

    composeTestRule.onNodeWithText("Accept friend request").assertIsDisplayed()
    composeTestRule.onNodeWithText("Decline friend request").assertIsDisplayed()
  }

  @Test
  fun testSentFriendRequestVersion() {
    // User is not a friend and not in friend requests, but in sentFriendRequests
    every { userViewModel.getUserFriends() } returns MutableStateFlow(emptyList())
    every { userViewModel.getUserFriendRequests() } returns MutableStateFlow(emptyList())
    every { userViewModel.getSentFriendRequests() } returns MutableStateFlow(listOf(friend.value))

    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }

    // Now should show "Requested" button with testTag "unsendFriendRequestButton"
    composeTestRule.onNodeWithTag("unsendFriendRequestButton").assertIsDisplayed()
  }

  @Test
  fun testNoRelationVersion() {
    // Not a friend, not friend request, not sent request
    every { userViewModel.getUserFriends() } returns MutableStateFlow(emptyList())
    every { userViewModel.getUserFriendRequests() } returns MutableStateFlow(emptyList())
    every { userViewModel.getSentFriendRequests() } returns MutableStateFlow(emptyList())

    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }

    // Should show "Send Friend Request"
    composeTestRule.onNodeWithTag("addToContactsButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addToContactsButton").performClick()
  }

  @Test
  fun testBlockUserDialog() {
    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("moreOptionsButton").assertIsDisplayed().performClick()

    composeTestRule.onNodeWithTag("blockUserButton").assertIsDisplayed().performClick()

    // Verify dialog content
    composeTestRule.onNodeWithText("Block User").assertIsDisplayed()
    composeTestRule.onNodeWithText("Are you sure you want to block this user?").assertIsDisplayed()
  }

  @Test
  fun testBlockUserDialogDismiss() {
    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("moreOptionsButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("blockUserButton").assertIsDisplayed().performClick()

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
    composeTestRule.onNodeWithTag("profileImage_1").assertIsDisplayed()
  }

  @Test
  fun testDeleteFriendSuccessFlow() {
    every { userViewModel.getUserFriends() } returns MutableStateFlow(listOf(friend.value))
    every { userViewModel.getUserFriendRequests() } returns MutableStateFlow(emptyList())
    every { userViewModel.getSentFriendRequests() } returns MutableStateFlow(emptyList())

    every {
      userViewModel.deleteFriend(
          user = any(), friend = any(), onSuccess = captureLambda(), onFailure = any())
    } answers
        {
          lambda<() -> Unit>().invoke() // Immediately call onSuccess
        }

    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }

    // Open menu and delete friend
    composeTestRule.onNodeWithTag("moreOptionsButton").performClick()
    composeTestRule.onNodeWithTag("deleteFriendButton").performClick()

    verify { navigationActions.goBack() }
  }

  @Test
  fun testAcceptFriendRequestSuccessFlow() {
    every { userViewModel.getUserFriends() } returns MutableStateFlow(emptyList())
    every { userViewModel.getUserFriendRequests() } returns MutableStateFlow(listOf(friend.value))
    every { userViewModel.getSentFriendRequests() } returns MutableStateFlow(emptyList())

    every {
      userViewModel.acceptFriendRequest(
          user = any(), friend = any(), onSuccess = captureLambda(), onFailure = any())
    } answers { lambda<() -> Unit>().invoke() }

    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }

    // Accept friend request
    composeTestRule.onNodeWithText("Accept friend request").performClick()
    // onSuccess called, scenario covered.
  }

  @Test
  fun testDeclineFriendRequestSuccessFlow() {
    every { userViewModel.getUserFriends() } returns MutableStateFlow(emptyList())
    every { userViewModel.getUserFriendRequests() } returns MutableStateFlow(listOf(friend.value))
    every { userViewModel.getSentFriendRequests() } returns MutableStateFlow(emptyList())

    every {
      userViewModel.declineFriendRequest(
          user = any(), friend = any(), onSuccess = captureLambda(), onFailure = any())
    } answers { lambda<() -> Unit>().invoke() }

    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }

    // Decline friend request
    composeTestRule.onNodeWithText("Decline friend request").performClick()
    // onSuccess called, scenario covered.
  }

  @Test
  fun testBlockUserSuccessFlow() {
    every { userViewModel.getUserFriends() } returns MutableStateFlow(listOf(friend.value))
    every { userViewModel.getUserFriendRequests() } returns MutableStateFlow(emptyList())
    every { userViewModel.getSentFriendRequests() } returns MutableStateFlow(emptyList())

    every {
      userViewModel.blockUser(
          user = any(), blockedUser = any(), onSuccess = captureLambda(), onFailure = any())
    } answers { lambda<() -> Unit>().invoke() }

    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }

    // Open dialog to block user
    composeTestRule.onNodeWithTag("moreOptionsButton").performClick()
    composeTestRule.onNodeWithTag("blockUserButton").performClick()

    // Click "Yes" to confirm blocking
    composeTestRule.onNodeWithText("Yes").performClick()

    verify { navigationActions.goBack() }
  }

  @Test
  fun testDeleteFriendOption() {
    every { userViewModel.getUserFriends() } returns MutableStateFlow(listOf(friend.value))
    every { userViewModel.getUserFriendRequests() } returns MutableStateFlow(emptyList())
    every { userViewModel.getSentFriendRequests() } returns MutableStateFlow(emptyList())

    composeTestRule.setContent { ContactScreen(navigationActions, userViewModel) }

    // Open the dropdown menu
    composeTestRule.onNodeWithTag("moreOptionsButton").performClick()

    // Verify "Delete Friend" option is displayed
    composeTestRule.onNodeWithTag("deleteFriendButton").assertIsDisplayed()

    // Perform click on "Delete Friend"
    composeTestRule.onNodeWithTag("deleteFriendButton").performClick()

    // Verify that deleteFriend was called with correct arguments
    verify {
      userViewModel.deleteFriend(
          user = match { it.uid == currentUser.value.uid },
          friend = match { it.uid == friend.value.uid },
          onSuccess = any(),
          onFailure = any())
    }
  }
}
