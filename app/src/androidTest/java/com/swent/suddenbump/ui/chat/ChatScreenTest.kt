package com.swent.suddenbump.ui.chat

import android.location.Location
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.swent.suddenbump.model.chat.Message
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.TopLevelDestination
import com.swent.suddenbump.ui.utils.defaultUserOnlineValue
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // **Mock variables declared as properties of the test class**
  private lateinit var mockViewModel: UserViewModel
  private lateinit var fakeNavigationActions: FakeNavigationActions
  private lateinit var mockNavController: NavHostController
  private val location =
      Location("dummy").apply {
        latitude = 0.0
        longitude = 0.0
      }

  // **Set up method to initialize mocks before each test**
  @Before
  fun setUp() {
    // Initialize the mocked NavHostController
    mockNavController = mockk(relaxed = true)

    // Initialize the FakeNavigationActions with the mocked NavHostController
    fakeNavigationActions = FakeNavigationActions(mockNavController)

    // Initialize the mocked UserViewModel
    mockViewModel = mockk(relaxed = true)
  }

  // Adjusted FakeNavigationActions
  class FakeNavigationActions(navController: NavHostController) : NavigationActions(navController) {
    var goBackCalled = false

    override fun navigateTo(destination: TopLevelDestination) {
      // Do nothing or track calls if needed
    }

    override fun navigateTo(screen: String) {
      // Do nothing or track calls if needed
    }

    override fun goBack() {
      goBackCalled = true
    }

    override fun currentRoute(): String {
      return "" // Return an empty string or a test route
    }
  }

  @Test
  fun testChatScreenDisplaysMessages() {
    // **Arrange**

    // Set up initial messages
    val message1 =
        Message(
            senderId = "currentUserId",
            content = "Hello",
            timestamp = Timestamp(1620000000, 0),
            isReadBy = listOf())
    val message2 =
        Message(
            senderId = "otherUserId",
            content = "Hi there",
            timestamp = Timestamp(1620003600, 0),
            isReadBy = listOf())
    val messagesFlow = MutableStateFlow<List<Message>>(listOf(message1, message2))
    every { mockViewModel.messages } returns messagesFlow

    // Set current user
    val currentUser =
        User("currentUserId", "Test", "User", "123456789", null, "test@example.com", location)
    val currentUserFlow = MutableStateFlow<User>(currentUser)
    every { mockViewModel.getCurrentUser() } returns currentUserFlow

    // Set other user
    val otherUser =
        User("otherUserId", "Other", "User", "987654321", null, "other@example.com", location)
    every { mockViewModel.user } returns otherUser

    // **Act**
    composeTestRule.setContent {
      ChatScreen(viewModel = mockViewModel, navigationActions = fakeNavigationActions)
    }

    composeTestRule.waitForIdle()

    // **Assert**
    composeTestRule.onNodeWithText("Hello").assertIsDisplayed()
    composeTestRule.onNodeWithText("Hi there").assertIsDisplayed()
  }

  @Test
  fun testBackButtonCallsGoBack() {
    // **Arrange**

    // Since we already initialized mockViewModel and fakeNavigationActions in setUp(),
    // we can directly use them here.

    val otherUser =
        User("otherUserId", "Other", "User", "987654321", null, "other@example.com", location)
    every { mockViewModel.user } returns otherUser

    // **Act**
    composeTestRule.setContent {
      ChatScreen(viewModel = mockViewModel, navigationActions = fakeNavigationActions)
    }

    // Click the back button
    composeTestRule.onNodeWithContentDescription("Back").performClick()

    // **Assert**
    assert(fakeNavigationActions.goBackCalled)
  }

  @Test
  fun testFriendIsOnline_DisplaysOnlineText() {
    // **Arrange**

    defaultUserOnlineValue = true

    // Define the user and the other friend
    val uid = "friend_id"
    val user =
        User(
            uid = "current_user_id",
            firstName = "Current",
            lastName = "User",
            phoneNumber = "+1234567890",
            profilePicture = null,
            emailAddress = "current.user@example.com",
            lastKnownLocation = location)
    val otherUser =
        User(
            uid = uid,
            firstName = "Friend",
            lastName = "User",
            phoneNumber = "+0987654321",
            profilePicture = null,
            emailAddress = "friend.user@example.com",
            lastKnownLocation = location)

    // Mock the ViewModel's user property to return otherUser
    every { mockViewModel.user } returns otherUser

    // Mock getCurrentUser to return the current user
    val currentUserFlow = MutableStateFlow(user)
    every { mockViewModel.getCurrentUser() } returns currentUserFlow

    // Mock getUserStatus to invoke onSuccess with true (Online)
    every { mockViewModel.getUserStatus(eq(uid), any(), any()) } answers
        {
          val onSuccess = it.invocation.args[1] as (Boolean) -> Unit
          onSuccess(true) // Friend is online
          Unit
        }

    // **Act**
    composeTestRule.setContent {
      ChatScreen(viewModel = mockViewModel, navigationActions = fakeNavigationActions)
    }

    // Allow all pending coroutines to execute
    composeTestRule.waitForIdle()

    // **Assert**
    composeTestRule.onNodeWithText("Online").assertIsDisplayed()
    composeTestRule.onNodeWithText("Offline").assertDoesNotExist()

    defaultUserOnlineValue = false
  }

  @Test
  fun testFriendIsOffline_DisplaysOfflineText() {
    // **Arrange**

    defaultUserOnlineValue = false

    // Define the user and the other friend
    val uid = "friend_id"
    val user =
        User(
            uid = "current_user_id",
            firstName = "Current",
            lastName = "User",
            phoneNumber = "+1234567890",
            profilePicture = null,
            emailAddress = "current.user@example.com",
            lastKnownLocation = location)
    val otherUser =
        User(
            uid = uid,
            firstName = "Friend",
            lastName = "User",
            phoneNumber = "+0987654321",
            profilePicture = null,
            emailAddress = "friend.user@example.com",
            lastKnownLocation = location)

    // Mock the ViewModel's user property to return otherUser
    every { mockViewModel.user } returns otherUser

    // Mock getCurrentUser to return the current user
    val currentUserFlow = MutableStateFlow(user)
    every { mockViewModel.getCurrentUser() } returns currentUserFlow

    // Mock getUserStatus to invoke onSuccess with false (Offline)
    every { mockViewModel.getUserStatus(eq(uid), any(), any()) } answers
        {
          val onSuccess = it.invocation.args[1] as (Boolean) -> Unit
          onSuccess(false) // Friend is offline
          Unit
        }

    // **Act**
    composeTestRule.setContent {
      ChatScreen(viewModel = mockViewModel, navigationActions = fakeNavigationActions)
    }

    // Allow all pending coroutines to execute
    composeTestRule.waitForIdle()

    // **Assert**
    composeTestRule.onNodeWithText("Offline").assertIsDisplayed()
    composeTestRule.onNodeWithText("Online").assertDoesNotExist()
  }
}
