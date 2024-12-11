package com.swent.suddenbump.ui.overview

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class AccountScreenTest {

  @Mock
  private lateinit var navigationActions: NavigationActions
  @Mock
  private lateinit var userViewModel: UserViewModel
  @Mock
    private lateinit var userRepository: UserRepository
    @Mock
    private lateinit var chatRepository: ChatRepository

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    userRepository = mock(UserRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    chatRepository = mock(ChatRepository::class.java)

    doNothing().`when`(userRepository).logoutUser()

    userViewModel = UserViewModel(userRepository, chatRepository)
    composeTestRule.setContent {
      AccountScreen(navigationActions = navigationActions, userViewModel = userViewModel)
    }
  }

  @Test
  fun hasRequiredComponents() {
    // Verify that the top bar title "Account" is displayed
    composeTestRule.onNodeWithText("Account").assertIsDisplayed()

    // Verify that each key section is displayed
    composeTestRule.onNodeWithTag("languageSection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteAccountSection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("logoutSection").assertIsDisplayed()
  }

  @Test
  fun goBackButtonCallsNavActions() {
    // Perform a click on the back button and verify that the goBack navigation action is triggered
    composeTestRule.onNodeWithTag("backButton").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun languageButtonOpensLanguageMenu() {
    // Perform a click on the "Language" section
    composeTestRule.onNodeWithTag("languageSection").performClick()

    // Verify that the dropdown menu appears
    composeTestRule.onNodeWithTag("languageMenuItem_English").assertIsDisplayed()
    composeTestRule.onNodeWithTag("languageMenuItem_French").assertIsDisplayed()
    composeTestRule.onNodeWithTag("languageMenuItem_German").assertIsDisplayed()
  }

  @Test
  fun deleteAccountButtonNavigatesToAccountScreen() {
    // Perform a click on the "Delete Account" section
    composeTestRule.onNodeWithTag("deleteAccountSection").performClick()
    verify(navigationActions).navigateTo("AccountScreen")
  }

  @Test
  fun testLogoutSectionVisibility() {
    // Assert that the logout section is displayed
    composeTestRule.onNodeWithTag("logoutSection").assertIsDisplayed()
  }

  @Test
  fun testLogoutButtonCallsLogout() {
    // Perform a click on the "Log out" section
    composeTestRule.onNodeWithTag("logoutSection").assertHasClickAction()
    composeTestRule.onNodeWithTag("logoutSection").performClick()
    verify(navigationActions).navigateTo(Route.AUTH)
  }

}