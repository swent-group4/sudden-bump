package com.swent.suddenbump.ui.overview

import android.location.Location
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import com.swent.suddenbump.model.user.DistanceCategory
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import com.swent.suddenbump.ui.navigation.Screen
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
        latitude = 37.7749
        longitude = -122.4194 // San Francisco
      }

  private val location2 =
      Location("mock_provider").apply {
        latitude = 40.7128
        longitude = -74.0060 // New York
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
          profilePicture = ImageBitmap(100, 100),
          emailAddress = "alice.brown@example.com",
          lastKnownLocation = MutableStateFlow(location3))

  @Before
  fun setUp() {
    navigationActions = mockk(relaxed = true)
    every { navigationActions.currentRoute() } returns Route.OVERVIEW

    userViewModel = mockk(relaxed = true)

    // Mock user and friends
    every { userViewModel.getCurrentUser() } returns MutableStateFlow(user1)
    every { userViewModel.groupedFriends } returns
        MutableStateFlow(
            mapOf(
                DistanceCategory.WITHIN_5KM to listOf(user1 to 1000f),
                DistanceCategory.WITHIN_10KM to listOf(user2 to 8000f)))

    // Mock location fetching
    coEvery { userViewModel.getCityAndCountry(any()) } returns
        "San Francisco, USA" andThen
        "New York, USA"
  }

  @Test
  fun testRequiredComponentsAreDisplayed() {
    composeTestRule.setContent { OverviewScreen(navigationActions, userViewModel) }
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settingsFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("seeFriendsFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("appName").assertIsDisplayed()
  }

  @Test
  fun testDisplaysFriendsWithinCategories() {
    composeTestRule.setContent { OverviewScreen(navigationActions, userViewModel) }
    composeTestRule.waitForIdle()

    composeTestRule.onRoot(useUnmergedTree = true).printToLog("Debug")

    composeTestRule.onNodeWithTag("Within 5km").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Within 10km").assertIsDisplayed()

    composeTestRule.onNodeWithTag(user1.uid).assertIsDisplayed()
    composeTestRule.onNodeWithTag(user2.uid).assertIsDisplayed()

    composeTestRule.onNodeWithText("San Francisco, USA").assertIsDisplayed()
    composeTestRule.onNodeWithText("New York, USA").assertIsDisplayed()
  }

  @Test
  fun testLoadingState() {
    // Simulate loading state
    every { userViewModel.groupedFriends } returns MutableStateFlow(null)

    composeTestRule.setContent { OverviewScreen(navigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("loadingFriends").assertIsDisplayed()
  }

  @Test
  fun testEmptyState() {
    // Simulate empty state
    every { userViewModel.groupedFriends } returns MutableStateFlow(emptyMap())

    composeTestRule.setContent { OverviewScreen(navigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("noFriends").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("No friends nearby, add friends to see their location")
        .assertIsDisplayed()
  }

  @Test
  fun testSettingsButtonNavigatesToSettings() {
    composeTestRule.setContent { OverviewScreen(navigationActions, userViewModel) }
    composeTestRule.onNodeWithTag("settingsFab").performClick()
    verify { navigationActions.navigateTo(Screen.SETTINGS) }
  }

  @Test
  fun testAddContactButtonNavigatesToAddContact() {
    composeTestRule.setContent { OverviewScreen(navigationActions, userViewModel) }
    composeTestRule.onNodeWithTag("seeFriendsFab").performClick()
    verify { navigationActions.navigateTo(Screen.ADD_CONTACT) }
  }

  @Test
  fun havingProfilePictureDisplaysComponent() {
    every { userViewModel.groupedFriends } returns
        MutableStateFlow(
            mapOf(
                DistanceCategory.WITHIN_5KM to listOf(user1 to 1000f),
                DistanceCategory.WITHIN_10KM to listOf(user2 to 8000f),
                DistanceCategory.FURTHER to listOf(user3 to 150000f)))

    composeTestRule.setContent { OverviewScreen(navigationActions, userViewModel) }
    composeTestRule.waitForIdle()

    // Verify the profile picture image
    composeTestRule
        .onNodeWithTag("profileImage_${user2.uid}", useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("profileImageNotNull_${user3.uid}", useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun testUserRowClickNavigatesToContact() {
    composeTestRule.setContent { OverviewScreen(navigationActions, userViewModel) }
    composeTestRule.onNodeWithTag(user1.uid).performClick()
    verify { navigationActions.navigateTo(Screen.CONTACT) }
    verify { userViewModel.setSelectedContact(user1) }
  }
}
