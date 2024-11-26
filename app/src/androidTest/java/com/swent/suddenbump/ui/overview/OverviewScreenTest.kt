// OverviewScreenTest.kt

package com.swent.suddenbump.ui.overview

import android.location.Location
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.model.user.UserViewModel.DistanceRange
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import com.swent.suddenbump.ui.navigation.Screen
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OverviewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var userViewModel: UserViewModel

  private val location1 =
      Location("mock_provider").apply {
        latitude = 46.5180
        longitude = 6.5680
      }

  private val location2 =
      Location("mock_provider").apply {
        latitude = 46.5180
        longitude = 6.5681
      }

  private val location3 =
      Location("mock_provider").apply {
        latitude = 46.5190
        longitude = 6.5680
      }

  private val user1 =
      User(
          uid = "1",
          firstName = "John",
          lastName = "Doe",
          phoneNumber = "+1234567890",
          profilePicture = null,
          emailAddress = "john.doe@example.com",
          lastKnownLocation = MutableStateFlow(location1))

  private val user2 =
      User(
          uid = "2",
          firstName = "Jane",
          lastName = "Smith",
          phoneNumber = "+1234567891",
          profilePicture = null,
          emailAddress = "jane.smith@example.com",
          lastKnownLocation = MutableStateFlow(location2))

  private val user3 =
      User(
          uid = "3",
          firstName = "Alice",
          lastName = "Brown",
          phoneNumber = "+1234567892",
          profilePicture = null,
          emailAddress = "alice.brown@example.com",
          lastKnownLocation = MutableStateFlow(location3))

  @Before
  fun setUp() {
    // Initialize MockK
    MockKAnnotations.init(this, relaxed = true)

    // Mock NavigationActions
    navigationActions = mockk(relaxed = true)
    every { navigationActions.currentRoute() } returns Route.OVERVIEW

    // Mock UserViewModel
    userViewModel = mockk(relaxed = true)

    // Mock getCurrentUser to return user1
    val currentUserFlow = MutableStateFlow(user1)
    every { userViewModel.getCurrentUser() } returns currentUserFlow

    // Mock friendsGroupedByDistance to group user2 and user3 appropriately
    val friendsGroupedMap =
        mapOf(
            DistanceRange.WITHIN_5KM to listOf(user2), DistanceRange.WITHIN_10KM to listOf(user3)
            // You can add more groups if needed
            )
    every { userViewModel.friendsGroupedByDistance } returns MutableStateFlow(friendsGroupedMap)

    // Mock startUpdatingFriendsLocations to do nothing (since it's handled by LaunchedEffect)
    every { userViewModel.startUpdatingFriendsLocations() } just Runs

    // Set the composable content for testing
    composeTestRule.setContent { OverviewScreen(navigationActions, userViewModel) }
  }

  @Test
  fun hasRequiredComponents() {
    // Assert that the main components are displayed
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settingsFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("seeFriendsFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("appName").assertIsDisplayed()
  }

  @Test
  fun displaysFriendsWithinCategories() {
    // Optionally, print the UI tree for debugging
    // composeTestRule.onRoot().printToLog("UI_TREE")

    // Check that the expected categories are displayed
    composeTestRule.onNodeWithTag("Within 5km").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Within 10km").assertIsDisplayed()

    // Verify that other categories are not displayed
    composeTestRule.onNodeWithTag("Within 20km").assertDoesNotExist()
    composeTestRule.onNodeWithTag("Further").assertDoesNotExist()

    // Check that the friends are displayed under the correct categories
    composeTestRule.onNodeWithTag(user2.uid).assertIsDisplayed()
    composeTestRule.onNodeWithTag(user3.uid).assertIsDisplayed()
  }

  @Test
  fun settingsButtonCallsNavActions() {
    // Perform click on the settings FAB
    composeTestRule.onNodeWithTag("settingsFab").performClick()

    // Verify that navigation to SETTINGS screen was triggered
    verify { navigationActions.navigateTo(Screen.SETTINGS) }
  }

  @Test
  fun addContactButtonCallsNavActions() {
    // Perform click on the "See Friends" FAB
    composeTestRule.onNodeWithTag("seeFriendsFab").performClick()

    // Verify that navigation to ADD_CONTACT screen was triggered
    verify { navigationActions.navigateTo(Screen.ADD_CONTACT) }
  }

  @Test
  fun clickingFriendNavigatesToContactScreen() {
    // Arrange: Mock setSelectedContact to do nothing
    every { userViewModel.setSelectedContact(any()) } just Runs

    // Act: Perform click on user2's row
    composeTestRule.onNodeWithTag(user2.uid).performClick()

    // Assert:
    // 1. setSelectedContact is called with user2
    verify { userViewModel.setSelectedContact(match { it.uid == user2.uid }) }

    // 2. Navigation to CONTACT screen is triggered
    verify { navigationActions.navigateTo(Screen.CONTACT) }
  }

  /*@Test
  fun showsLoadingIndicatorWhenNoFriends() {
      // Arrange: Mock friendsGroupedByDistance to be empty
      every { userViewModel.friendsGroupedByDistance } returns MutableStateFlow(emptyMap())

      // Recompose the UI with the updated state
      composeTestRule.setContent {
          OverviewScreen(navigationActions, userViewModel)
      }

      // Assert that the CircularProgressIndicator is displayed
      composeTestRule.onNode(
          hasTestTag("overviewScreen")
                  and hasDescendant(isInstanceOf(androidx.compose.material3.CircularProgressIndicator::class.java))
      ).assertIsDisplayed()
  }*/

}
