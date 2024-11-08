package com.swent.suddenbump.ui

import android.location.Location
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.authentication.SignInScreen
import com.swent.suddenbump.ui.authentication.SignUpScreen
import com.swent.suddenbump.ui.chat.ChatScreen
import com.swent.suddenbump.ui.contact.AddContactScreen
import com.swent.suddenbump.ui.contact.ContactScreen
import com.swent.suddenbump.ui.map.MapScreen
import com.swent.suddenbump.ui.messages.MessagesScreen
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import com.swent.suddenbump.ui.navigation.Screen
import com.swent.suddenbump.ui.overview.ConversationScreen
import com.swent.suddenbump.ui.overview.FriendsListScreen
import com.swent.suddenbump.ui.overview.OverviewScreen
import com.swent.suddenbump.ui.settings.SettingsScreen
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class endToEndTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var auth: FirebaseAuth

  @Before
  fun setUp() {
    val mockUserViewModel = mockk<UserViewModel>(relaxed = true)
    // Mock user account data
    val mockUser =
        User(
            uid = "testUid",
            firstName = "Test",
            lastName = "User",
            phoneNumber = "123-456-7890",
            profilePicture = null, // Replace with an ImageBitmap if needed
            emailAddress = "test.user@example.com",
            lastKnownLocation =
                Location("mockProvider").apply {
                  latitude = 37.7749
                  longitude = -122.4194
                })

    // Set up mock data directly in the test without modifying UserRepositoryFirestore
    val mockUserStateFlow: StateFlow<User> = MutableStateFlow(mockUser)
    every { mockUserViewModel.getCurrentUser() } returns mockUserStateFlow

    // Initialize Firebase and sign out any existing user for a fresh start
    auth = FirebaseAuth.getInstance()
    auth.signOut()
  }

  @Test
  fun testEndToEndFullAppNavigation() {
    // Step 1: Start at SignInScreen and perform sign-in
    composeTestRule.setContent {
      val location: Location? = null
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)

      NavHost(navController = navController, startDestination = Route.AUTH) {
        navigation(
            startDestination = Screen.AUTH,
            route = Route.AUTH,
        ) {
          composable(Screen.AUTH) { SignInScreen(navigationActions, userViewModel) }
          composable(Screen.SIGNUP) { SignUpScreen(navigationActions, userViewModel) }
        }
        navigation(
            startDestination = Screen.OVERVIEW,
            route = Route.OVERVIEW,
        ) {
          composable(Screen.OVERVIEW) { OverviewScreen(navigationActions, userViewModel) }
          composable(Screen.FRIENDS_LIST) { FriendsListScreen(navigationActions, userViewModel) }
          composable(Screen.ADD_CONTACT) { AddContactScreen(navigationActions, userViewModel) }
          composable(Screen.CONV) { ConversationScreen(navigationActions) }
          composable(Screen.SETTINGS) { SettingsScreen(navigationActions) }
          composable(Screen.CONTACT) { ContactScreen(navigationActions, userViewModel) }
          composable(Screen.CHAT) { ChatScreen(userViewModel, navigationActions) }
        }

        navigation(
            startDestination = Screen.MAP,
            route = Route.MAP,
        ) {
          composable(Screen.MAP) { MapScreen(navigationActions, location, userViewModel) }
        }
        navigation(
            startDestination = Screen.MESS,
            route = Route.MESS,
        ) {
          composable(Screen.MESS) { MessagesScreen(userViewModel, navigationActions) }
        }
      }
    }

    composeTestRule.waitForIdle()
    // Check that the main screen container is visible
    composeTestRule.onNodeWithTag("loginButton").assertExists()
    // Simulate user interaction for authentication
    composeTestRule.onNodeWithTag("loginButton").performClick()
    composeTestRule.waitForIdle()
    // Verify that the user is navigated to the Overview screen
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    // Step 2: Navigate to Friends List
    composeTestRule.onNodeWithTag("seeFriendsFab").performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("friendsListScreen").assertExists()

    // Step 3: Navigate to Add Contact screen
    composeTestRule.onNodeWithTag("addContactButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("addContactScreen").assertExists()

    // Step 4: Navigate to Contact screen
    composeTestRule.onNodeWithTag("userList").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("contactScreen").assertExists()

    // Step 5: Navigate back to Overview
    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("addContactScreen").assertExists()
    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("friendsListScreen").assertExists()

    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    // Step 6: Navigate to Settings screen
    composeTestRule.onNodeWithTag("settingsFab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settingsScreen").assertExists()

    // Step 7: Navigate back to Overview
    composeTestRule.onNodeWithTag("customGoBackButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    // Step 8: Navigate to Map screen
    composeTestRule.onNodeWithTag("Map").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("mapView").assertExists()

    // Step 9: Navigate to Messages screen
    composeTestRule.onNodeWithTag("Messages").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("messages_list").assertExists()
  }
}
