package com.swent.suddenbump.ui.overview

import android.location.Location
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import com.swent.suddenbump.ui.navigation.Screen
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class OverviewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var userRepository: UserRepository
  private lateinit var chatRepository: ChatRepository
  private lateinit var userViewModel: UserViewModel

  private val location1 =
      MutableStateFlow(
          Location("mock_provider").apply {
            latitude = 46.5180
            longitude = 6.5680
          })

  private val location2 =
      MutableStateFlow(
          Location("mock_provider").apply {
            latitude = 46.5180
            longitude = 6.5681
          })

  private val location3 =
      MutableStateFlow(
          Location("mock_provider").apply {
            latitude = 46.5190
            longitude = 6.5680
          })

  private val user1 =
      User(
          uid = "1",
          firstName = "John",
          lastName = "Doe",
          phoneNumber = "+1234567890",
          profilePicture = null,
          emailAddress = "john.doe@example.com",
          lastKnownLocation = location1)

  private val user2 =
      User(
          uid = "2",
          firstName = "Jane",
          lastName = "Smith",
          phoneNumber = "+1234567891",
          profilePicture = null,
          emailAddress = "jane.smith@example.com",
          lastKnownLocation = location2)

  private val user3 =
      User(
          uid = "3",
          firstName = "Alice",
          lastName = "Brown",
          phoneNumber = "+1234567892",
          profilePicture = null,
          emailAddress = "alice.brown@example.com",
          lastKnownLocation = location3)

  @Before
  fun setUp() {
    // Mock FirebaseFirestore
    mockkStatic(FirebaseFirestore::class)
    val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)
    every { FirebaseFirestore.getInstance() } returns mockFirestore

    // Mock NavigationActions
    navigationActions = mockk(relaxed = true)
    every { navigationActions.currentRoute() } returns Route.OVERVIEW

    // Mock UserRepository
    userRepository = mockk(relaxed = true)
    // Mock getUserFriends to immediately invoke onSuccess with listOf(user2, user3)
    every { userRepository.getUserFriends(any(), any(), any()) } answers
        {
          val onSuccess = arg<(List<User>) -> Unit>(1)
          onSuccess.invoke(listOf(user2, user3))
        }

    // Mock getFriendsLocation to immediately invoke onSuccess with friends' locations
    every { userRepository.getFriendsLocation(any(), any(), any()) } answers
        {
          val friends = firstArg<List<User>>()
          val onSuccess = arg<(Map<User, Location?>) -> Unit>(1)
          val friendsLocations = friends.associateWith { it.lastKnownLocation.value }
          onSuccess.invoke(friendsLocations)
        }

    // Instantiate UserViewModel with mocked repository
    chatRepository = mockk(relaxed = true)
    userViewModel = UserViewModel(repository = userRepository, chatRepository = chatRepository)

    // Set the current user
    userViewModel.setUser(user1, onSuccess = {}, onFailure = {})

    // Load friends and their locations
    userViewModel.setCurrentUser()
    userViewModel.loadFriendsLocations()

    // Mock getRelativeDistance to return specific distances
    every { userViewModel.getRelativeDistance(user2) } returns 1000f // Within 5km
    every { userViewModel.getRelativeDistance(user3) } returns 8000f // Within 10km

    // Now set the content for the test
    composeTestRule.setContent { OverviewScreen(navigationActions, userViewModel) }
  }

  @After
  fun tearDown() {
    unmockkAll()
    Dispatchers.resetMain()
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
    // Add this line before your assertions
    composeTestRule.onRoot().printToLog("UI_TREE")
    // Proceed with assertions
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

@ExperimentalCoroutinesApi
class MainDispatcherRule(val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) :
    TestWatcher() {
  override fun starting(description: Description?) {
    Dispatchers.resetMain()
  }
}
