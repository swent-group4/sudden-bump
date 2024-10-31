package com.swent.suddenbump.ui.contacts

import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.contact.AddContactScreen
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AddContactScreenTest {

  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private val exception = Exception()

  private lateinit var navigationActions: NavigationActions
  @get:Rule val composeTestRule = createComposeRule()

  private val user =
    User("1", "Martin", "Vetterli", "+41 00 000 00 01", null, "martin.vetterli@epfl.ch")

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)

    userRepository = mock(UserRepository::class.java)
    userViewModel = UserViewModel(userRepository)

    `when`(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)
    composeTestRule.setContent { AddContactScreen(navigationActions, userViewModel, true) }

    doAnswer { invocationOnMock ->
      Log.i("AddContact", "InvocationMocked?")
      val query = invocationOnMock.getArgument<(String) -> Unit>(0)
      val onSuccess = invocationOnMock.getArgument<(List<User>) -> Unit>(1)
      val onFailure = invocationOnMock.getArgument<(Exception) -> Unit>(2)
      query
      onSuccess(listOf(user))
      onFailure(exception)
    }.whenever(userRepository).searchQueryAddContact(anyString(), any(), any())
  }

  @Test
  fun testInitialScreenState() {
    // Verify the top bar title
    composeTestRule.onNodeWithText("Add contact").assertIsDisplayed()

    // Verify the search field is displayed
    composeTestRule.onNodeWithTag("searchTextField").assertIsDisplayed()

    // Verify the recommended row is displayed
    composeTestRule.onNodeWithTag("recommendedRow").assertIsDisplayed()

    // Verify the list of users is displayed
    composeTestRule.onNodeWithTag("noUsersText").assertIsDisplayed()
  }

  @Test
  fun testSearchFunctionality() {

    // Enter a search query
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("Martin")

    // Verify that the list is filtered
    composeTestRule.onNodeWithText("Martin Vetterli").assertIsDisplayed()
    composeTestRule.onNodeWithText("Jane Smith").assertDoesNotExist()
  }

  @Test
  fun testNoResultsMessage() {
    // Enter a search query that yields no results
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("NonExistentName")

    // Verify that the no results message is displayed
    composeTestRule
        .onNodeWithText("Looks like no user corresponds to your query")
        .assertIsDisplayed()
  }

  @Test
  fun testSearchFunctionalityCaseInsensitivity() {
    // Enter a search query in uppercase
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("JOHN")

    // Verify that "John Doe" is displayed
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
  }

  @Test
  fun testEmptyUserListDisplaysNoUsersMessage() {

    // Clear the search field to ensure no users are shown
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("qowve0iwqnev0oiwnev")

    // Verify that the 'no users' message is displayed
    composeTestRule
        .onNodeWithText("Looks like no user corresponds to your query")
        .assertIsDisplayed()
  }

  @Test
  fun testNavigationBackButton() {
    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("backButton").performClick()
    verify(navigationActions).goBack()
  }
}
