// package com.swent.suddenbump.ui.overview
//
// import androidx.compose.ui.test.*
// import androidx.compose.ui.test.junit4.createComposeRule
// import com.swent.suddenbump.ui.navigation.NavigationActions
// import com.swent.suddenbump.ui.navigation.Route
// import com.swent.suddenbump.ui.navigation.Screen
// import org.junit.Before
// import org.junit.Rule
// import org.junit.Test
// import org.mockito.Mockito.mock
// import org.mockito.Mockito.`when`
// import org.mockito.kotlin.verify
//
// class FriendsListTest {
//  private lateinit var navigationActions: NavigationActions
//  @get:Rule val composeTestRule = createComposeRule()
//
//  @Before
//  fun setUp() {
//    navigationActions = mock(NavigationActions::class.java)
//
//    `when`(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)
//    composeTestRule.setContent { FriendsListScreen(navigationActions) }
//  }
//
//  @Test
//  fun testInitialScreenState() {
//    // Verify the top bar title
//    composeTestRule.onNodeWithText("Friends").assertIsDisplayed()
//
//    // Verify the search field is displayed
//    composeTestRule.onNodeWithTag("searchTextField").assertIsDisplayed()
//
//    // Verify the list of users is displayed
//    composeTestRule.onNodeWithTag("userList").assertIsDisplayed()
//  }
//
//  @Test
//  fun testScaffoldDisplay() {
//    composeTestRule.onNodeWithTag("friendsListScreen").assertIsDisplayed()
//    // Verify the top bar title
//    composeTestRule.onNodeWithText("Friends").assertIsDisplayed()
//    // Verify the search field is displayed
//    composeTestRule.onNodeWithTag("searchTextField").assertIsDisplayed()
//    // Verify the list of users is displayed
//    composeTestRule.onNodeWithTag("userList").assertIsDisplayed()
//  }
//
//  @Test
//  fun testSearchFunctionality() {
//    // Enter a search query
//    composeTestRule.onNodeWithTag("searchTextField").performTextInput("John")
//
//    // Verify that the list is filtered
//    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
//    composeTestRule.onNodeWithText("Jane Smith").assertDoesNotExist()
//  }
//
//  @Test
//  fun testNoResultsMessage() {
//    // Enter a search query that yields no results
//    composeTestRule.onNodeWithTag("searchTextField").performTextInput("NonExistentName")
//
//    // Verify that the no results message is displayed
//    composeTestRule
//        .onNodeWithText("Looks like no user corresponds to your query")
//        .assertIsDisplayed()
//  }
//
//  @Test
//  fun backButtonCallsNavActions() {
//    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
//    composeTestRule.onNodeWithTag("backButton").performClick()
//    verify(navigationActions).goBack()
//  }
//
//  @Test
//  fun addContactButtonCallsNavActions() {
//    composeTestRule.onNodeWithTag("addContactButton").assertIsDisplayed()
//    composeTestRule.onNodeWithTag("addContactButton").performClick()
//    verify(navigationActions).navigateTo(Screen.ADD_CONTACT)
//  }
// }
