package com.swent.suddenbump.ui.overview

import android.location.Location
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BlockedUsersScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var userViewModel: UserViewModel
  private lateinit var currentUser: User
  private lateinit var blockedUser1: User
  private lateinit var blockedUser2: User

  @Before
  fun setUp() {
    navigationActions = mockk(relaxed = true)
    userViewModel = mockk(relaxed = true)

    // Create a mock location
    val mockLocation =
        Location("mock_provider").apply {
          latitude = 46.5180
          longitude = 6.5680
        }

    // Set up test users
    currentUser =
        User(
            uid = "current_user",
            firstName = "Current",
            lastName = "User",
            phoneNumber = "+41789123450",
            profilePicture = null,
            emailAddress = "current.user@example.com",
            lastKnownLocation = mockLocation)

    blockedUser1 =
        User(
            uid = "blocked1",
            firstName = "John",
            lastName = "Doe",
            phoneNumber = "+41789123456",
            profilePicture = null,
            emailAddress = "john.doe@example.com",
            lastKnownLocation = mockLocation)

    blockedUser2 =
        User(
            uid = "blocked2",
            firstName = "Jane",
            lastName = "Smith",
            phoneNumber = "+41789123457",
            profilePicture = null,
            emailAddress = "jane.smith@example.com",
            lastKnownLocation = mockLocation)

    // Mock navigation and view model responses
    every { navigationActions.currentRoute() } returns Route.OVERVIEW
    every { userViewModel.getCurrentUser() } returns MutableStateFlow(currentUser)
    every { userViewModel.getBlockedFriends() } returns
        MutableStateFlow(listOf(blockedUser1, blockedUser2))
  }

  @Test
  fun testInitialScreenState() {
    composeTestRule.setContent { BlockedUsersScreen(navigationActions, userViewModel) }

    // Verify screen is displayed
    composeTestRule.onNodeWithTag("blockedUsersScreen").assertExists()

    // Verify top bar
    composeTestRule.onNodeWithText("Blocked Users").assertExists()
    composeTestRule.onNodeWithTag("backButton").assertExists()

    // Verify blocked users are displayed
    composeTestRule.onNodeWithText("John Doe").assertExists()
    composeTestRule.onNodeWithText("Jane Smith").assertExists()
  }

  @Test
  fun testEmptyBlockedUsersList() {
    // Mock empty blocked users list
    every { userViewModel.getBlockedFriends() } returns MutableStateFlow(emptyList())

    composeTestRule.setContent { BlockedUsersScreen(navigationActions, userViewModel) }

    // Verify empty state message
    composeTestRule.onNodeWithText("You haven't blocked any users").assertExists()
  }

  @Test
  fun testNavigationBackButton() {
    composeTestRule.setContent { BlockedUsersScreen(navigationActions, userViewModel) }

    // Click back button
    composeTestRule.onNodeWithTag("backButton").performClick()

    // Verify navigation
    verify { navigationActions.goBack() }
  }

  @Test
  fun testUnblockUser() {
    composeTestRule.setContent { BlockedUsersScreen(navigationActions, userViewModel) }

    // Click unblock button for the first user
    composeTestRule.onAllNodesWithTag("unblockButton")[0].performClick()

    // Verify confirmation dialog appears
    composeTestRule.onNodeWithText("Unblock User").assertExists()
    composeTestRule.onNodeWithText("Are you sure you want to unblock John Doe?").assertExists()
  }

  @Test
  fun testUnblockUserConfirmation() {
    composeTestRule.setContent { BlockedUsersScreen(navigationActions, userViewModel) }

    // Click unblock button for the first user
    composeTestRule.onAllNodesWithTag("unblockButton")[0].performClick()

    // Verify confirmation dialog appears
    composeTestRule.onNodeWithText("Unblock User").assertExists()
    composeTestRule.onNodeWithText("Are you sure you want to unblock John Doe?").assertExists()

    // Click 'Yes' to confirm
    composeTestRule.onNodeWithText("Yes").performClick()

    // Verify unblock action was called
    verify { userViewModel.unblockUser(blockedUser1, any(), any()) }
  }

  @Test
  fun testUnblockUserCancellation() {
    composeTestRule.setContent { BlockedUsersScreen(navigationActions, userViewModel) }

    // Click unblock button for the first user
    composeTestRule.onAllNodesWithTag("unblockButton")[0].performClick()

    // Verify confirmation dialog appears
    composeTestRule.onNodeWithText("Unblock User").assertExists()
    composeTestRule.onNodeWithText("Are you sure you want to unblock John Doe?").assertExists()

    // Click 'No' to cancel
    composeTestRule.onNodeWithText("No").performClick()

    // Verify dialog is dismissed
    composeTestRule.onNodeWithText("Unblock User").assertDoesNotExist()

    // Verify unblock was not called
    verify(exactly = 0) { userViewModel.unblockUser(any(), any(), any()) }
  }

  @Test
  fun testBlockedUserItemDisplay() {
    composeTestRule.setContent { BlockedUsersScreen(navigationActions, userViewModel) }

    // Verify user items are properly displayed
    composeTestRule.onAllNodesWithTag("blockedUserItem").assertCountEquals(2)

    // Verify each user's information is displayed
    composeTestRule.onNodeWithText("John Doe").assertExists()
    composeTestRule.onNodeWithText("Jane Smith").assertExists()
  }
}
