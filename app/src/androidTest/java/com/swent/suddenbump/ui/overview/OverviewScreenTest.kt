package com.swent.suddenbump.ui.overview

import android.location.Location
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import com.swent.suddenbump.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify

class OverviewScreenTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var repository: UserRepository
  private lateinit var chatRepository: ChatRepository
  private lateinit var userViewModel: UserViewModel

  private val location =
      Location("mock_provider").apply {
        latitude = 0.0
        longitude = 0.0
      }

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    repository = mock(UserRepository::class.java)
    chatRepository = mock(ChatRepository::class.java)
    userViewModel = UserViewModel(repository = repository, chatRepository = chatRepository)

    //    val currentUser = User(uid="1", firstName="John", lastName="Doe", phoneNumber =
    // "0610136713", emailAddress = "john.doe@example.com", profilePicture = null, lastKnownLocation
    // = location)
    //    val users = listOf(
    //      User(uid="2", firstName="Jane", lastName="Doe", phoneNumber = "0667977556", emailAddress
    // = "jane.doe@example.com", profilePicture = null, lastKnownLocation = location),
    //      User(uid="3", firstName="Alice", lastName="Smith", phoneNumber = "0612345678",
    // emailAddress = "alice.smith@example.com", profilePicture = null, lastKnownLocation =
    // location),
    //    )
    //
    //    val currentUserStateFlow = MutableStateFlow(currentUser)
    //    val usersStateFlow = MutableStateFlow(users)
    //
    //    `when`(userViewModel.getCurrentUser()).thenReturn(currentUserStateFlow)
    //    `when`(userViewModel.getUserFriends()).thenReturn(usersStateFlow)

    `when`(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)

    composeTestRule.setContent { OverviewScreen(navigationActions, userViewModel) }
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settingsFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("seeFriendsFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("appName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("distanceTitle").assertIsDisplayed()
  }

  @Test
  fun settingsButtonCallsNavActions() {
    composeTestRule.onNodeWithTag("settingsFab").performClick()
    verify(navigationActions).navigateTo(Screen.SETTINGS)
  }

  @Test
  fun addContactButtonCallsNavActions() {
    composeTestRule.onNodeWithTag("seeFriendsFab").performClick()
    verify(navigationActions).navigateTo(Screen.ADD_CONTACT)
  }

  //  @Test
  //  fun userListIsDisplayed() {
  //    composeTestRule.onNodeWithTag("userList").assertIsDisplayed()
  //  }
  //
  //  @Test
  //  fun noFriendsMessageIsDisplayedWhenNoFriends() {
  //    val emptyListStateFlow = MutableStateFlow(emptyList<User>())
  //    `when`(userViewModel.getUserFriends()).thenReturn(emptyListStateFlow)
  //    composeTestRule.setContent {
  //      OverviewScreen(navigationActions, userViewModel)
  //    }
  //    composeTestRule.onNodeWithTag("noFriends").assertIsDisplayed()
  //  }
}
