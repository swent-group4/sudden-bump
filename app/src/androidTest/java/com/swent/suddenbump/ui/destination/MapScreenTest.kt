package com.swent.suddenbump.ui.destination

import android.location.Location
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.meeting.MeetingRepository
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.map.MapScreen
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.TopLevelDestinations
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class MapScreenTest {

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var chatRepository: ChatRepository
  private lateinit var meetingViewModel: MeetingViewModel
  private lateinit var meetingRepository: MeetingRepository

  private val exception = Exception()
  private val location =
      MutableStateFlow(
          Location("mock_provider").apply {
            latitude = 0.0
            longitude = 0.0
          })
  private val user =
      User("1", "Martin", "Vetterli", "+41 00 000 00 01", null, "martin.vetterli@epfl.ch", location)

  @Before
  fun setUp() {
    userRepository = mock(UserRepository::class.java)
    chatRepository = mock(ChatRepository::class.java)
    meetingRepository = mock(MeetingRepository::class.java)
    meetingViewModel = MeetingViewModel(meetingRepository)
    userViewModel = UserViewModel(userRepository, chatRepository)
  }

  @Test
  fun mapScreen_displaysMapAndBottomNavigation() {
    // Create a NavHostController using rememberNavController
    composeTestRule.setContent {
      val navController = rememberNavController()

      // Create NavigationActions with the real NavHostController
      val navigationActions = NavigationActions(navController)

      // Set the content for testing
      MapScreen(
          navigationActions = navigationActions,
          userViewModel = userViewModel,
          meetingViewModel = meetingViewModel)
    }

    // Verify that the bottom navigation is displayed
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertExists()

    // Verify that the "Overview" tab exists in the bottom navigation
    composeTestRule
        .onNodeWithTag("Overview") // This corresponds to the textId of the tab
        .assertExists()

    // Verify that the "Map" tab exists in the bottom navigation
    composeTestRule
        .onNodeWithTag("Map") // This corresponds to the textId of the tab
        .assertExists()

    // Verify that the map is displayed (use content description or tag from SimpleMap)
    composeTestRule
        .onNodeWithTag("mapView") // Ensure you have a testTag for SimpleMap in your composable
        .assertExists()
  }

  @Test
  fun mapScreen_onTabSelect_callsNavigateTo() {
    // Mock the NavigationActions
    val mockNavigationActions = mock(NavigationActions::class.java)

    // Ensure that currentRoute returns a valid, non-null route
    Mockito.`when`(mockNavigationActions.currentRoute()).thenReturn("Overview")

    // Set the content for testing
    composeTestRule.setContent {
      MapScreen(
          navigationActions = mockNavigationActions,
          userViewModel = userViewModel,
          meetingViewModel = meetingViewModel)
    }

    // Simulate a click on the "Map" tab (or any other tab based on your setup)
    composeTestRule
        .onNodeWithTag("Map") // Assuming you have a testTag on each tab
        .performClick()

    // Verify that the navigateTo method was called with the "Map" TopLevelDestination
    verify(mockNavigationActions).navigateTo(TopLevelDestinations.MAP)
  }
}
