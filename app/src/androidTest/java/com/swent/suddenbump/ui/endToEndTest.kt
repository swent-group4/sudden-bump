package com.swent.suddenbump.ui

import android.location.Location
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.suddenbump.MainActivity
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  private val location =
      Location("mockProvider").apply {
        latitude = 37.7749
        longitude = -122.4194
      }

  @Before
  fun setUp() {
    val mockUserViewModel = mockk<UserViewModel>(relaxed = true)
    val mockUser =
        User(
            uid = "testUid",
            firstName = "Test",
            lastName = "User",
            phoneNumber = "123-456-7890",
            profilePicture = null, // Replace with an ImageBitmap if needed
            emailAddress = "test.user@example.com",
            lastKnownLocation = location)

    val mockUserStateFlow: StateFlow<User> = MutableStateFlow(mockUser)
    every { mockUserViewModel.getCurrentUser() } returns mockUserStateFlow

    // You may need to mock more methods depending on how your ViewModel is used
  }

  @Test
  fun testEndToEndFullAppNavigation() {
    // Wait for the app to load
    composeTestRule.waitForIdle()

    // Step 1: Simulate user interaction for authentication
    composeTestRule.onNodeWithTag("loginButton").assertExists().performClick()
    composeTestRule.waitForIdle()
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
