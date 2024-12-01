package com.swent.suddenbump.ui

import android.location.Location
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.swent.suddenbump.MainActivity
import com.swent.suddenbump.model.chat.ChatRepositoryFirestore
import com.swent.suddenbump.model.chat.Message
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  private lateinit var mockFirestore: FirebaseFirestore
  private lateinit var mockAuth: FirebaseAuth
  private lateinit var chatRepository: ChatRepositoryFirestore
  private lateinit var mockMessagesCollection: CollectionReference
  private lateinit var mockMessageDocument: DocumentReference
  private lateinit var mockQuery: Query
  private lateinit var mockFirebaseUser: FirebaseUser
  private lateinit var mockUserViewModel: UserViewModel

  private val location =
      Location("mockProvider").apply {
        latitude = 37.7749
        longitude = -122.4194
      }

  @Before
  fun setUp() {
    // Mock Firestore, FirebaseAuth, and other dependencies
    mockFirestore = mockk(relaxed = true)
    mockAuth = mockk(relaxed = true)
    chatRepository = mockk(relaxed = true)
    mockQuery = mockk(relaxed = true)
    mockFirebaseUser = mockk(relaxed = true)
    mockUserViewModel = mockk(relaxed = true)

    val currentUserId = "user2"
    val friendId = "1"
    val chatId = "chat789"
    val mockQuerySnapshot = mockk<QuerySnapshot>(relaxed = true)
    val mockChatQuerySnapshot = mockk<QuerySnapshot>(relaxed = true)
    val mockChatDocumentSnapshot = mockk<DocumentSnapshot>(relaxed = true)
    val mockDocumentSnapshot1 = mockk<DocumentSnapshot>(relaxed = true)
    val mockDocumentSnapshot2 = mockk<DocumentSnapshot>(relaxed = true)

    val message1 = Message("msg1", friendId, "Hello", Timestamp.now(), listOf(friendId))
    val message2 = Message("msg2", currentUserId, "Hi", Timestamp.now(), listOf(currentUserId))

    mockMessagesCollection = mockk(relaxed = true)
    mockMessageDocument = mockk(relaxed = true)
    val mockChatsCollection = mockk<CollectionReference>(relaxed = true)
    val mockChatDocument = mockk<DocumentReference>(relaxed = true)
    val mockMessagesSubCollection = mockk<CollectionReference>(relaxed = true)

    every { mockChatsCollection.whereArrayContains("participants", friendId) } returns mockQuery
    every { mockQuery.get() } returns Tasks.forResult(mockChatQuerySnapshot)
    every { mockChatQuerySnapshot.documents } returns listOf(mockChatDocumentSnapshot)
    every { mockChatDocumentSnapshot.id } returns chatId
    every { mockChatDocumentSnapshot.get("participants") } returns listOf(friendId, currentUserId)

    every { mockFirestore.collection("chats") } returns mockChatsCollection
    every { mockChatsCollection.document(any<String>()) } returns mockChatDocument
    every { mockChatDocument.collection("messages") } returns mockMessagesSubCollection
    every { mockMessagesSubCollection.orderBy("timestamp", Query.Direction.DESCENDING) } returns
        mockQuery

    every { mockQuery.get() } returns Tasks.forResult(mockQuerySnapshot)
    every { mockQuerySnapshot.documents } returns
        listOf(mockDocumentSnapshot1, mockDocumentSnapshot2)
    every { mockDocumentSnapshot1.toObject(Message::class.java) } returns message1
    every { mockDocumentSnapshot2.toObject(Message::class.java) } returns message2

    every { mockUserViewModel.getUserFriends() } returns
        MutableStateFlow(
            listOf(User("1", "John", "Doe", "+1234567890", null, "", MutableStateFlow(location))))
  }

  @Test
  fun fullAppNavigationTest() {
    composeTestRule.waitForIdle()

    // Step 1: Simulate user interaction for authentication
    composeTestRule.onNodeWithTag("loginButton").assertExists().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    // Step 2: Navigate to Friends List
    composeTestRule.onNodeWithTag("seeFriendsFab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("addContactScreen").assertExists()

    // Step 3: Wait for and navigate to Contact screen
    //    composeTestRule.waitUntil(timeoutMillis = 10_000) {
    //      try {
    //        composeTestRule.onNodeWithTag("userList").assertExists()
    //        true
    //      } catch (e: AssertionError) {
    //        false
    //      }
    //    }
    //    composeTestRule.onNodeWithTag("userList").performClick()
    //    composeTestRule.waitForIdle()
    //    composeTestRule.onNodeWithTag("contactScreen").assertExists()
    //
    //    // Step 4: Navigate back to Overview
    //    composeTestRule.onNodeWithTag("backButton").performClick()
    //    composeTestRule.waitForIdle()
    //    composeTestRule.onNodeWithTag("addContactScreen").assertExists()
    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    // Step 5: Navigate to Settings screen
    composeTestRule.onNodeWithTag("settingsFab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settingsScreen").assertExists()
  }

  /* @Test
  fun testSendMessageEoE() {

    composeTestRule.waitForIdle()

    // Step 1: Simulate user interaction for authentication
    composeTestRule.onNodeWithTag("loginButton").assertExists().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    // part to debug, instead of clicking on a user's profile on the overview, go through
    // conversation scree/message screen
     // Step 2: Navigate to user row and send message
    composeTestRule.onAllNodes(isRoot()).printToLog("ComposeTree")

    composeTestRule.onNodeWithTag("1").assertExists().performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("sendMessageButton").performClick()

    composeTestRule.onNodeWithTag("ChatInputTextBox").performTextInput("Hello, how are you?")
    composeTestRule.onNodeWithTag("SendButton").performClick()
    composeTestRule.waitForIdle()

    composeTestRule
      .onNodeWithTag("ChatInputTextBox")
      .performTextInput("Do you want to meet at Rolex today at 10?")
    composeTestRule.onNodeWithTag("SendButton").performClick()
    composeTestRule.waitForIdle()
  }*/
}
