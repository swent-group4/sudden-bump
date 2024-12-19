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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
  private lateinit var flowMock: Flow<List<ChatSummary>>

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

  private val location =
      Location("mock_provider").apply {
        latitude = 0.0
        longitude = 0.0
      }

  private val chatSummaryDummy =
      ChatSummary("chat1", "this is a message1", "chat456", Timestamp.now(), 0, listOf("1", "2"))
  private val chatSummaryDummy2 =
      ChatSummary(
          "chat2",
          "this is a message2",
          "chat456",
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
    flowMock = flowOf(listOf(chatSummaryDummy, chatSummaryDummy2))

    userViewModel.setUser(userDummy2, {}, {})
    userViewModel.setUserFriends(userDummy2, listOf(userDummy1, userDummy3), {}, {})

    `when`(navigationActions.currentRoute()).thenReturn(Route.MESS)

    whenever(chatRepository.getChatSummaries(userDummy2.uid)).thenAnswer { flowMock }

    composeTestRule.setContent { MessagesScreen(userViewModel, navigationActions) }
  }

  @Test
  fun hasRequiredComponents() {
    // Wait for the Screen to be fully displayed before running tests
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("messages_list").assertIsDisplayed()
    composeTestRule.onNodeWithTag("divider").assertIsDisplayed()
    composeTestRule.onNodeWithTag("message_item_${chatSummaryDummy.sender}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("message_item_${chatSummaryDummy2.sender}").assertIsDisplayed()
  }
}
