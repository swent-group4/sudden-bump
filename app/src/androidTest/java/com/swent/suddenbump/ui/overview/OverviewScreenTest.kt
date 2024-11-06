package com.swent.suddenbump.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    userRepository = mock(UserRepository::class.java)
    userViewModel = UserViewModel(userRepository)

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
  }

  @Test
  fun settingsButtonCallsNavActions() {
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settingsFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settingsFab").performClick()
    verify(navigationActions).navigateTo(screen = Screen.SETTINGS)
  }

  @Test
  fun addContactButtonCallsNavActions() {
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("seeFriendsFab").assertIsDisplayed()
    composeTestRule.onNodeWithTag("seeFriendsFab").performClick()
    verify(navigationActions).navigateTo(screen = Screen.FRIENDS_LIST)
  }

  @Test
  fun userListIsDisplayed() {
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("userList").assertIsDisplayed()
  }
}
