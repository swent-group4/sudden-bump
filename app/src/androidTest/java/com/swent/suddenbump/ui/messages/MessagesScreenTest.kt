package com.swent.suddenbump.ui.messages

import android.location.Location
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.google.firebase.Timestamp
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.chat.ChatSummary
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepository
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.whenever

class MessagesScreenTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var repository: UserRepository
  private lateinit var chatRepository: ChatRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var flowMock: MutableStateFlow<List<ChatSummary>>

  private val locationDummy =
      Location("mock_provider").apply {
        latitude = 0.0 // Set latitude
        longitude = 0.0 // Set longitude
      }

  private val userDummy1 =
      User(
          "1",
          "Martin",
          "Vetterli",
          "+41 00 000 00 01",
          ImageBitmap(30, 30),
          "martin.vetterli@epfl.ch",
          locationDummy)
  private val userDummy2 =
      User(
          "2",
          "Martin",
          "Vetterli",
          "+41 00 000 00 01",
          ImageBitmap(30, 30),
          "martin.vetterli@epfl.ch",
          locationDummy)
  private val userDummy3 =
      User(
          "3",
          "Martine ",
          "Veto early",
          "+41 00 000 00 01",
          ImageBitmap(1500, 1500),
          "martin.vetterli@epfl.ch",
          locationDummy)

  private val chatSummaryDummy =
      ChatSummary("chat1", "this is a message1", "1", Timestamp.now(), 0, listOf("1", "2"))
  private val chatSummaryDummy2 =
      ChatSummary(
          "chat2",
          "this is a message2",
          "2",
          Timestamp(seconds = 1729515283, nanoseconds = 0),
          0,
          listOf("2", "3"))

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    repository = mock(UserRepository::class.java)
    chatRepository = mock(ChatRepository::class.java)
    userViewModel = UserViewModel(repository = repository, chatRepository = chatRepository)

    // Set the current user and friends
    userViewModel.setUser(userDummy2, {}, {})
    userViewModel.setUserFriends(userDummy2, listOf(userDummy1, userDummy3), {}, {})

    `when`(navigationActions.currentRoute()).thenReturn(Route.MESS)
  }

  @Test
  fun hasRequiredComponents() {
    // Arrange: mock the repository to return a non-empty list
    flowMock = MutableStateFlow(listOf(chatSummaryDummy, chatSummaryDummy2))
    whenever(chatRepository.getChatSummaries(userDummy2.uid)).thenReturn(flowMock)

    // Act: set the content after configuring the mock
    composeTestRule.setContent { MessagesScreen(userViewModel, navigationActions) }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Assert: check that all required components are displayed
    composeTestRule.onNodeWithTag("back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("messages_list").assertIsDisplayed()
    composeTestRule.onNodeWithTag("divider").assertIsDisplayed()
    composeTestRule.onNodeWithTag("message_item_${chatSummaryDummy.sender}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("message_item_${chatSummaryDummy2.sender}").assertIsDisplayed()
  }

  @Test
  fun showsNoMessagesTextWhenListIsEmpty() {
    // Arrange: mock the repository to return an empty list
    flowMock = MutableStateFlow(emptyList())
    whenever(chatRepository.getChatSummaries(userDummy2.uid)).thenReturn(flowMock)

    // Act: set the content after configuring the mock
    composeTestRule.setContent { MessagesScreen(userViewModel, navigationActions) }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Assert: check that the "no messages" text is displayed and the messages list is not
    composeTestRule.onNodeWithTag("no_messages_text").assertIsDisplayed()
    composeTestRule.onNodeWithTag("messages_list").assertDoesNotExist()
  }
}
