package com.swent.suddenbump.ui

import android.location.Location
import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.swent.suddenbump.MainActivity
import com.swent.suddenbump.model.chat.ChatRepositoryFirestore
import com.swent.suddenbump.model.meeting.Meeting
import com.swent.suddenbump.model.meeting.MeetingRepository
import com.swent.suddenbump.model.meeting.MeetingRepositoryFirestore
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.meeting_location.LocationViewModel
import com.swent.suddenbump.model.meeting_location.NominatimLocationRepository
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.authentication.SignInScreen
import com.swent.suddenbump.ui.authentication.SignUpScreen
import com.swent.suddenbump.ui.calendar.AddMeetingScreen
import com.swent.suddenbump.ui.calendar.CalendarMeetingsScreen
import com.swent.suddenbump.ui.calendar.EditMeetingScreen
import com.swent.suddenbump.ui.calendar.PendingMeetingsScreen
import com.swent.suddenbump.ui.chat.ChatScreen
import com.swent.suddenbump.ui.contact.AddContactScreen
import com.swent.suddenbump.ui.contact.ContactScreen
import com.swent.suddenbump.ui.map.MapScreen
import com.swent.suddenbump.ui.messages.MessagesScreen
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import com.swent.suddenbump.ui.navigation.Screen
import com.swent.suddenbump.ui.overview.AccountScreen
import com.swent.suddenbump.ui.overview.BlockedUsersScreen
import com.swent.suddenbump.ui.overview.FriendsListScreen
import com.swent.suddenbump.ui.overview.OverviewScreen
import com.swent.suddenbump.ui.overview.SettingsScreen
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest1 {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  /** Tests the full app navigation flow. */
  @Test
  fun fullAppNavigationTest() {
    composeTestRule.waitForIdle()

    // Step 1: Simulate user interaction for authentication
    composeTestRule.onNodeWithTag("loginButton").assertExists().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    // Step 2: Navigate to Friends List
    composeTestRule.onNodeWithTag("seeFriendsFab").performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("addContactScreen").assertExists()

    composeTestRule.waitForIdle()

    // Step 3: Navigate back to Overview
    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    // Step 5: Navigate to Settings screen
    composeTestRule.onNodeWithTag("settingsFab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settingsScreen").assertExists()

    composeTestRule.onNodeWithTag("settingsScreen").assertExists()
    // Step 6: Navigate back to Overview
    composeTestRule.onNodeWithTag("backButton").performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    composeTestRule.onNodeWithTag("Calendar").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("calendarMeetingsScreen").assertExists()
    composeTestRule.onNodeWithTag("pendingMeetingsButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("pendingMeetingsScreen").assertExists()
    composeTestRule.onNodeWithTag("Back").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("calendarMeetingsScreen").assertExists()
    composeTestRule.onNodeWithTag("Overview").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()
  }
}

@RunWith(AndroidJUnit4::class)
class EndToEndTests2and3 {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockFirestore: UserRepositoryFirestore
  private lateinit var mockChatRepository: ChatRepositoryFirestore
  private lateinit var mockQuery: Query
  private lateinit var userViewModel: UserViewModel
  private lateinit var meetingViewModel: MeetingViewModel
  private lateinit var mockMeetingRepositoryFirestore: MeetingRepositoryFirestore
  private lateinit var locationViewModel: LocationViewModel
  private lateinit var mockLocationRepository: NominatimLocationRepository

  @Before
  fun setUp() {
    // Mock Firestore, FirebaseAuth, and other dependencies
    mockFirestore = mockk(relaxed = true)
    mockChatRepository = mockk(relaxed = true)
    mockQuery = mockk(relaxed = true)
    mockMeetingRepositoryFirestore = mockk(relaxed = true)
    mockLocationRepository = mockk(relaxed = true)
    meetingViewModel = MeetingViewModel(mockMeetingRepositoryFirestore)
    locationViewModel = LocationViewModel(mockLocationRepository)

    userViewModel = UserViewModel(mockFirestore, mockChatRepository)

    // Define mock user and friend
    val userLocation =
        Location("provider").apply {
          latitude = 37.7749 // San Francisco
          longitude = -122.4194
        }
    val friendLocation =
        Location("provider").apply {
          latitude = 37.7849 // Nearby in San Francisco
          longitude = -122.4094
        }

    val user =
        User(
            uid = "user1",
            firstName = "Test",
            lastName = "User",
            phoneNumber = "1234567890",
            profilePicture = null,
            emailAddress = "test.user@example.com",
            lastKnownLocation = userLocation)

    val friend =
        User(
            uid = "1",
            firstName = "Friend",
            lastName = "User",
            phoneNumber = "0987654321",
            profilePicture = null,
            emailAddress = "friend.user@example.com",
            lastKnownLocation = friendLocation)

    // Mock getUserFriends to return a single friend
    every { mockFirestore.getUserFriends(any(), captureLambda(), any()) } answers
        {
          lambda<(List<User>) -> Unit>().invoke(listOf(friend))
        }

    // Mock getUserAccount to set the user
    every { mockFirestore.getUserAccount(captureLambda(), any()) } answers
        {
          lambda<(User) -> Unit>().invoke(user)
        }

    every { mockFirestore.getSharedWithFriends(any(), captureLambda(), any()) } answers
        {
          lambda<(List<User>) -> Unit>().invoke(listOf(friend))
        }

    // Trigger initialization of the UserViewModel
    userViewModel.setCurrentUser()
  }

  //    @After
  //    fun tearDown() {
  //        // Clean up
  //        mockFirestore
  //        mockChatRepository
  //        mockQuery
  //        userViewModel
  //        meetingViewModel
  //        mockMeetingRepositoryFirestore
  //        locationViewModel
  //        mockLocationRepository
  //    }

  /** Tests the end-to-end flow of sending a message. */
  @Test
  fun endToEndTest2() {

    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      Log.d("TAG", "userViewModel userFriends: ${userViewModel.getUserFriends().value}")

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
          composable(Screen.OVERVIEW) {
            // Start permission requests
            OverviewScreen(navigationActions, userViewModel)
          }
          composable(Screen.FRIENDS_LIST) { FriendsListScreen(navigationActions, userViewModel) }
          composable(Screen.ADD_CONTACT) { AddContactScreen(navigationActions, userViewModel) }
          composable(Screen.SETTINGS) {
            SettingsScreen(navigationActions, userViewModel, meetingViewModel)
          }
          composable(Screen.CONTACT) {
            ContactScreen(navigationActions, userViewModel, meetingViewModel)
          }
          composable(Screen.CHAT) { ChatScreen(userViewModel, navigationActions) }
          composable(Screen.ADD_MEETING) {
            AddMeetingScreen(navigationActions, userViewModel, meetingViewModel)
          }
        }
        navigation(
            startDestination = Screen.CALENDAR,
            route = Route.CALENDAR,
        ) {
          composable(Screen.CALENDAR) {
            CalendarMeetingsScreen(navigationActions, meetingViewModel, userViewModel)
          }
          composable(Screen.EDIT_MEETING) { EditMeetingScreen(navigationActions, meetingViewModel) }
          composable(Screen.PENDING_MEETINGS) {
            PendingMeetingsScreen(navigationActions, meetingViewModel, userViewModel)
          }
        }

        navigation(
            startDestination = Screen.MAP,
            route = Route.MAP,
        ) {
          composable(Screen.MAP) { MapScreen(navigationActions, userViewModel, meetingViewModel) }
        }
        navigation(
            startDestination = Screen.MESS,
            route = Route.MESS,
        ) {
          composable(Screen.MESS) { MessagesScreen(userViewModel, navigationActions) }
        }

        // Add new screens from Settings.kt
        composable("AccountScreen") { AccountScreen(navigationActions, userViewModel) }
      }
    }

    composeTestRule.waitForIdle()

    // Step 1: Simulate user interaction for authentication
    composeTestRule.onNodeWithTag("loginButton").assertExists().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    composeTestRule.waitForIdle()

    // conversation screen/message screen
    // Step 2: Navigate to user row and send message

    composeTestRule.onNodeWithTag("1").assertExists().performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("sendMessageButton").performClick()

    composeTestRule.onNodeWithTag("ChatInputTextBox").performTextInput("Hello, how are you?")
    composeTestRule.onNodeWithTag("SendButton").performClick()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag("ChatInputTextBox")
        .performTextInput("Do you want to meet at Rolex today at 10?")
    composeTestRule.onNodeWithTag("SendButton").performClick()
    composeTestRule.waitForIdle()
  }

  /** Tests the end-to-end flow of blocking a friend. */
  @Test
  fun endToEndTest3() {

    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      Log.d("TAG", "userViewModel userFriends: ${userViewModel.getUserFriends().value}")

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
          composable(Screen.SETTINGS) {
            SettingsScreen(navigationActions, userViewModel, meetingViewModel)
          }
          composable(Screen.ACCOUNT) { AccountScreen(navigationActions, userViewModel) }
          composable(Screen.BLOCKED_USERS) { BlockedUsersScreen(navigationActions, userViewModel) }
          composable(Screen.CONTACT) {
            ContactScreen(navigationActions, userViewModel, meetingViewModel)
          }
          composable(Screen.CHAT) { ChatScreen(userViewModel, navigationActions) }
          composable(Screen.ADD_MEETING) {
            AddMeetingScreen(navigationActions, userViewModel, meetingViewModel, locationViewModel)
          }
        }
        navigation(
            startDestination = Screen.CALENDAR,
            route = Route.CALENDAR,
        ) {
          composable(Screen.CALENDAR) {
            CalendarMeetingsScreen(navigationActions, meetingViewModel, userViewModel)
          }
          composable(Screen.EDIT_MEETING) {
            EditMeetingScreen(navigationActions, meetingViewModel, locationViewModel)
          }
          composable(Screen.PENDING_MEETINGS) {
            PendingMeetingsScreen(navigationActions, meetingViewModel, userViewModel)
          }
        }

        navigation(
            startDestination = Screen.MAP,
            route = Route.MAP,
        ) {
          composable(Screen.MAP) { MapScreen(navigationActions, userViewModel, meetingViewModel) }
        }
        navigation(
            startDestination = Screen.MESS,
            route = Route.MESS,
        ) {
          composable(Screen.MESS) { MessagesScreen(userViewModel, navigationActions) }
        }
      }
    }

    // Step 1: Simulate user interaction for authentication
    composeTestRule.onNodeWithTag("loginButton").assertExists().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    composeTestRule.waitForIdle()

    // block user friend
    // Step 2: Navigate to settings and go to blocked users screen

    composeTestRule.onNodeWithTag("1").assertExists().performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("moreOptionsButton").performClick()

    composeTestRule.onNodeWithTag("blockUserButton").performClick()
    composeTestRule.onNodeWithTag("blockUserConfirmButton").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("backButton").performClick()

    composeTestRule.onNodeWithTag("overviewScreen").assertExists()
    composeTestRule.onNodeWithTag("settingsFab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("BlockedUsersOption").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("blockedUsersScreen").assertExists()
  }
}

@RunWith(AndroidJUnit4::class)
class EndToEndTest4 {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockFirestore: UserRepositoryFirestore
  private lateinit var meetingRepository: MeetingRepository
  private lateinit var mockChatRepository: ChatRepositoryFirestore
  private lateinit var mockQuery: Query
  private lateinit var userViewModel: UserViewModel
  private lateinit var meetingViewModel: MeetingViewModel
  private lateinit var locationViewModel: LocationViewModel
  private lateinit var mockLocationRepository: NominatimLocationRepository

  @Before
  fun setUp() {
    // Mock Firestore, FirebaseAuth, and other dependencies
    mockFirestore = mockk(relaxed = true)
    mockChatRepository = mockk(relaxed = true)
    mockQuery = mockk(relaxed = true)
    mockLocationRepository = mockk(relaxed = true)
    meetingRepository = mockk(relaxed = true)
    meetingViewModel = MeetingViewModel(meetingRepository)
    locationViewModel = LocationViewModel(mockLocationRepository)

    userViewModel = UserViewModel(mockFirestore, mockChatRepository)

    // Define mock user and friend
    val userLocation =
        Location("provider").apply {
          latitude = 37.7749 // San Francisco
          longitude = -122.4194
        }
    val friendLocation =
        Location("provider").apply {
          latitude = 37.7849 // Nearby in San Francisco
          longitude = -122.4094
        }

    val user =
        User(
            uid = "user1",
            firstName = "Test",
            lastName = "User",
            phoneNumber = "1234567890",
            profilePicture = null,
            emailAddress = "test.user@example.com",
            lastKnownLocation = userLocation)

    val friend =
        User(
            uid = "1",
            firstName = "Friend",
            lastName = "User",
            phoneNumber = "0987654321",
            profilePicture = null,
            emailAddress = "friend.user@example.com",
            lastKnownLocation = friendLocation)

    // Mock meeting data
    val meetings =
        listOf(
            Meeting(
                "1",
                com.swent.suddenbump.model.meeting_location.Location(12.34, 56.78, "Central Park"),
                Timestamp.now(),
                "user1",
                "1",
                false),
            Meeting(
                "2",
                com.swent.suddenbump.model.meeting_location.Location(12.24, 56.78, "City Square"),
                Timestamp.now(),
                "user1",
                "1",
                false))

    // Mock getUserFriends to return a single friend
    every { mockFirestore.getUserFriends(any(), captureLambda(), any()) } answers
        {
          lambda<(List<User>) -> Unit>().invoke(listOf(friend))
        }

    // Mock getUserAccount to set the user
    every { mockFirestore.getUserAccount(captureLambda(), any()) } answers
        {
          lambda<(User) -> Unit>().invoke(user)
        }

    every { meetingRepository.getMeetings(any(), any()) } answers
        {
          val onSuccess = firstArg<(List<Meeting>) -> Unit>()
          onSuccess(meetings)
        }

    // Trigger initialization of the UserViewModel
    userViewModel.setCurrentUser()
    // Trigger initialization of the MeetingViewModel
    meetingViewModel.selectMeeting(meetings[0])
  }

  /** Tests the end-to-end flow of accepting and declining meetings. */
  @Test
  fun testAcceptAndDeclineMeetings() {

    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      Log.d("TAG", "userViewModel userFriends: ${userViewModel.getUserFriends().value}")

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
          composable(Screen.OVERVIEW) {
            // Start permission requests
            OverviewScreen(navigationActions, userViewModel)
          }
          composable(Screen.FRIENDS_LIST) { FriendsListScreen(navigationActions, userViewModel) }
          composable(Screen.ADD_CONTACT) { AddContactScreen(navigationActions, userViewModel) }
          composable(Screen.SETTINGS) {
            SettingsScreen(navigationActions, userViewModel, meetingViewModel)
          }
          composable(Screen.CONTACT) {
            ContactScreen(navigationActions, userViewModel, meetingViewModel)
          }
          composable(Screen.CHAT) { ChatScreen(userViewModel, navigationActions) }
          composable(Screen.ADD_MEETING) {
            AddMeetingScreen(navigationActions, userViewModel, meetingViewModel)
          }
        }
        navigation(
            startDestination = Screen.CALENDAR,
            route = Route.CALENDAR,
        ) {
          composable(Screen.CALENDAR) {
            CalendarMeetingsScreen(navigationActions, meetingViewModel, userViewModel)
          }
          composable(Screen.EDIT_MEETING) { EditMeetingScreen(navigationActions, meetingViewModel) }
          composable(Screen.PENDING_MEETINGS) {
            PendingMeetingsScreen(navigationActions, meetingViewModel, userViewModel)
          }
        }

        navigation(
            startDestination = Screen.MAP,
            route = Route.MAP,
        ) {
          composable(Screen.MAP) { MapScreen(navigationActions, userViewModel, meetingViewModel) }
        }
        navigation(
            startDestination = Screen.MESS,
            route = Route.MESS,
        ) {
          composable(Screen.MESS) { MessagesScreen(userViewModel, navigationActions) }
        }

        // Add new screens from Settings.kt
        composable("AccountScreen") { AccountScreen(navigationActions, userViewModel) }
      }
    }

    composeTestRule.waitForIdle()

    // Step 1: Simulate user interaction for authentication
    composeTestRule.onNodeWithTag("loginButton").assertExists().performClick()
    composeTestRule.waitForIdle()

    // Step 2:  Navigate to CalendarMeetings screen
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()
    composeTestRule.onNodeWithTag("Calendar").performClick()
    composeTestRule.waitForIdle()

    // Step 3: Navigate to PendingMeetings screen
    composeTestRule.onNodeWithTag("calendarMeetingsScreen").assertExists()
    composeTestRule.onNodeWithTag("pendingMeetingsButton").performClick()
    composeTestRule.waitForIdle()

    // Step 4: Accept a meeting
    composeTestRule.onNodeWithTag("pendingMeetingsScreen").assertExists()
    composeTestRule.onAllNodesWithTag("meetingCard")[0].assertExists()
    composeTestRule.onAllNodesWithTag("acceptButton")[0].performClick()
    composeTestRule.waitForIdle()

    // Step 5: Decline a meeting
    composeTestRule.onNodeWithTag("pendingMeetingsScreen").assertExists()
    composeTestRule.onAllNodesWithTag("meetingCard")[1].assertExists()
    composeTestRule.onAllNodesWithTag("denyButton")[1].performClick()
    composeTestRule.waitForIdle()
  }
}
