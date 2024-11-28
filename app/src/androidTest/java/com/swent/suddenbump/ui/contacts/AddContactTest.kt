package com.swent.suddenbump.ui.contacts

import android.location.Location
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.contact.AddContactScreen
import com.swent.suddenbump.ui.contact.UserRecommendedRow
import com.swent.suddenbump.ui.contact.UserRequestRow
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import com.swent.suddenbump.ui.navigation.Screen
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any

class AddContactScreenTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var userViewModel: UserViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mockk(relaxed = true)
    userViewModel = mockk(relaxed = true)

    val currentUser =
        User(
            uid = "0",
            firstName = "Alice",
            lastName = "Wonderland",
            phoneNumber = "+1234567890",
            profilePicture = null,
            emailAddress = "",
            lastKnownLocation =
                MutableStateFlow(
                    Location("mock_provider").apply {
                      latitude = 46.5180
                      longitude = 6.5680
                    }))
    val user1 =
        User(
            uid = "1",
            firstName = "John",
            lastName = "Doe",
            phoneNumber = "+1234567890",
            profilePicture = null,
            emailAddress = "",
            lastKnownLocation =
                MutableStateFlow(
                    Location("mock_provider").apply {
                      latitude = 46.5180
                      longitude = 6.5680
                    }))
    val user2 =
        User(
            uid = "2",
            firstName = "Jane",
            lastName = "Smith",
            phoneNumber = "+1234567891",
            profilePicture = null,
            emailAddress = "",
            lastKnownLocation =
                MutableStateFlow(
                    Location("mock_provider").apply {
                      latitude = 46.5180
                      longitude = 6.5681
                    }))
    every { navigationActions.currentRoute() } returns (Route.OVERVIEW)
    every { navigationActions.goBack() } returns Unit

    every { userViewModel.getCurrentUser() } returns MutableStateFlow(currentUser)
    every { userViewModel.getUserRecommendedFriends() } returns MutableStateFlow(listOf(user1))
    every { userViewModel.getUserFriendRequests() } returns MutableStateFlow(listOf(user2))
    every { userViewModel.getSentFriendRequests() } returns MutableStateFlow(emptyList())
    every { userViewModel.getBlockedFriends() } returns MutableStateFlow(emptyList())

    composeTestRule.setContent { AddContactScreen(navigationActions, userViewModel) }
  }

  @Test
  fun testInitialScreenState() {
    // Verify the top bar title
    composeTestRule.onNodeWithText("Add friends").assertIsDisplayed()

    // Verify the search field is displayed
    composeTestRule.onNodeWithTag("searchTextField").assertIsDisplayed()

    // Verify the list of users is displayed
    composeTestRule.onNodeWithTag("userList").assertIsDisplayed()
  }

  @Test
  fun testNoResultsMessage() {
    // Enter a search query that yields no results
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("NonExistentName")

    // Verify that the no results message is displayed
    composeTestRule
        .onNodeWithText("Looks like no user corresponds to your query")
        .assertIsDisplayed()
  }

  @Test
  fun testSearch() {
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("John")

    // Verify that there is a user with the name "John" in the list
    composeTestRule.onNodeWithTag("userList").assertIsDisplayed()
  }

  @Test
  fun testNoRequestWhenSearch() {
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("ABC")

    composeTestRule.onNodeWithTag("requestRow").assertIsNotDisplayed()
  }

  @Test
  fun testNavigationBackButton() {
    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("backButton").performClick()
    verify { navigationActions.goBack() }
  }
}

class UserRowsTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var userViewModel: UserViewModel
  private lateinit var currentUser: User
  private lateinit var testUser: User

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mockk(relaxed = true)
    userViewModel = mockk(relaxed = true)

    currentUser =
        User(
            uid = "0",
            firstName = "Alice",
            lastName = "Wonderland",
            phoneNumber = "+1234567890",
            profilePicture = null,
            emailAddress = "",
            lastKnownLocation =
                MutableStateFlow(
                    Location("mock_provider").apply {
                      latitude = 46.5180
                      longitude = 6.5680
                    }))

    testUser =
        User(
            uid = "1",
            firstName = "John",
            lastName = "Doe",
            phoneNumber = "+1234567891",
            profilePicture = null,
            emailAddress = "",
            lastKnownLocation =
                MutableStateFlow(
                    Location("mock_provider").apply {
                      latitude = 46.5181
                      longitude = 6.5681
                    }))

    every { navigationActions.currentRoute() } returns Route.OVERVIEW
    every { userViewModel.setSelectedContact(any()) } returns Unit
  }

  @Test
  fun testUserRecommendedRowInitialState() {
    composeTestRule.setContent {
      UserRecommendedRow(
          currentUser = currentUser,
          user = testUser,
          navigationActions = navigationActions,
          userViewModel = userViewModel,
          sentFriendRequests = emptyList(),
          friendRequests = emptyList())
    }

    composeTestRule.onNodeWithText("John D.").assertIsDisplayed()
  }

  @Test
  fun testUserRecommendedRowClickNavigation() {
    composeTestRule.setContent {
      UserRecommendedRow(
          currentUser = currentUser,
          user = testUser,
          navigationActions = navigationActions,
          userViewModel = userViewModel,
          sentFriendRequests = emptyList(),
          friendRequests = emptyList())
    }

    composeTestRule.onRoot().performClick()
    verify { userViewModel.setSelectedContact(testUser) }
    verify { navigationActions.navigateTo(Screen.CONTACT) }
  }

  @Test
  fun testUserRequestRowInitialState() {
    composeTestRule.setContent {
      UserRequestRow(
          currentUser = currentUser,
          user = testUser,
          navigationActions = navigationActions,
          userViewModel = userViewModel,
          requestList = mutableStateOf(listOf(testUser)))
    }

    composeTestRule.onNodeWithText("John D.").assertIsDisplayed()
    composeTestRule.onNodeWithTag("acceptButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("denyButton").assertIsDisplayed()
  }

  @Test
  fun testUserRequestRowAcceptFriend() {
    val requestList = mutableStateOf(listOf(testUser))

    composeTestRule.setContent {
      UserRequestRow(
          currentUser = currentUser,
          user = testUser,
          navigationActions = navigationActions,
          userViewModel = userViewModel,
          requestList = requestList)
    }

    composeTestRule.onNodeWithTag("acceptButton").performClick()
    verify { userViewModel.acceptFriendRequest(currentUser, testUser, any(), any()) }
  }

  @Test
  fun testUserRequestRowDeclineFriend() {
    val requestList = mutableStateOf(listOf(testUser))

    composeTestRule.setContent {
      UserRequestRow(
          currentUser = currentUser,
          user = testUser,
          navigationActions = navigationActions,
          userViewModel = userViewModel,
          requestList = requestList)
    }

    composeTestRule.onNodeWithTag("denyButton").performClick()
    verify { userViewModel.declineFriendRequest(currentUser, testUser, any(), any()) }
  }
}
