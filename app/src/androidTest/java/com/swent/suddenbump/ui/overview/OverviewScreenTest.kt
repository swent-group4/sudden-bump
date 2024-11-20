package com.swent.suddenbump.ui.overview

import android.location.Location
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import com.swent.suddenbump.ui.navigation.Screen
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OverviewScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navigationActions: NavigationActions
    private lateinit var userViewModel: UserViewModel

    private val location1 = Location("mock_provider").apply {
        latitude = 46.5180
        longitude = 6.5680
    }

    private val location2 = Location("mock_provider").apply {
        latitude = 46.5180
        longitude = 6.5681
    }

    private val location3 = Location("mock_provider").apply {
        latitude = 46.5190
        longitude = 6.5680
    }

    private val user1 = User(
        uid = "1",
        firstName = "John",
        lastName = "Doe",
        phoneNumber = "+1234567890",
        profilePicture = null,
        emailAddress = "john.doe@example.com",
        lastKnownLocation = MutableStateFlow(location1)
    )

    private val user2 = User(
        uid = "2",
        firstName = "Jane",
        lastName = "Smith",
        phoneNumber = "+1234567891",
        profilePicture = null,
        emailAddress = "jane.smith@example.com",
        lastKnownLocation = MutableStateFlow(location2)
    )

    private val user3 = User(
        uid = "3",
        firstName = "Alice",
        lastName = "Brown",
        phoneNumber = "+1234567892",
        profilePicture = null,
        emailAddress = "alice.brown@example.com",
        lastKnownLocation = MutableStateFlow(location3)
    )

    @Before
    fun setUp() {
        // Mock NavigationActions
        navigationActions = mockk(relaxed = true)
        every { navigationActions.currentRoute() } returns Route.OVERVIEW

        // Mock UserViewModel
        userViewModel = mockk(relaxed = true)

        // Prepare the data that UserViewModel methods will return
        val currentUserFlow = MutableStateFlow(user1)
        every { userViewModel.getCurrentUser() } returns currentUserFlow

        val userFriendsFlow = MutableStateFlow(listOf(user2, user3))
        every { userViewModel.getUserFriends() } returns userFriendsFlow

        // Set up getRelativeDistance() method
        every { userViewModel.getRelativeDistance(user2) } returns 1000f // Within 5km
        every { userViewModel.getRelativeDistance(user3) } returns 8000f // Within 10km

        // Now set the content for the test
        composeTestRule.setContent { OverviewScreen(navigationActions, userViewModel) }
    }

    @Test
    fun hasRequiredComponents() {
        composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settingsFab").assertIsDisplayed()
        composeTestRule.onNodeWithTag("seeFriendsFab").assertIsDisplayed()
        composeTestRule.onNodeWithTag("appName").assertIsDisplayed()
    }

    @Test
    fun displaysFriendsWithinCategories() {
        composeTestRule.onRoot().printToLog("UI_TREE")
        composeTestRule.onNodeWithTag("Within 5km").assertIsDisplayed()
        composeTestRule.onNodeWithTag("Within 10km").assertIsDisplayed()

        // Verify that "Within 20km" and "Further" categories are not displayed
        composeTestRule.onNodeWithTag("Within 20km").assertDoesNotExist()
        composeTestRule.onNodeWithTag("Further").assertDoesNotExist()

        composeTestRule.onNodeWithTag(user2.uid).assertIsDisplayed()
        composeTestRule.onNodeWithTag(user3.uid).assertIsDisplayed()
    }

    @Test
    fun settingsButtonCallsNavActions() {
        composeTestRule.onNodeWithTag("settingsFab").performClick()
        verify { navigationActions.navigateTo(Screen.SETTINGS) }
    }

    @Test
    fun addContactButtonCallsNavActions() {
        composeTestRule.onNodeWithTag("seeFriendsFab").performClick()
        verify { navigationActions.navigateTo(Screen.ADD_CONTACT) }
    }
}
