package com.swent.suddenbump.ui

import android.location.Location
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.swent.suddenbump.MainActivity
import com.swent.suddenbump.model.chat.ChatRepositoryFirestore
import com.swent.suddenbump.model.chat.Message
import io.mockk.every
import io.mockk.mockk
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

  private val location =
      Location("mockProvider").apply {
        latitude = 37.7749
        longitude = -122.4194
      }

  @Before
  fun setUp() {
    mockFirestore = mockk(relaxed = true)
    mockAuth = mockk(relaxed = true)
    chatRepository = mockk(relaxed = true)
    mockQuery = mockk(relaxed = true)
    mockFirebaseUser = mockk(relaxed = true)

    val currentUserId = "user2"
    val friendId = "1"
    val chatId = "chat789"
    val mockQuerySnapshot = mockk<QuerySnapshot>(relaxed = true)
    val mockChatQuerySnapshot = mockk<QuerySnapshot>(relaxed = true)
    val mockChatDocumentSnapshot = mockk<DocumentSnapshot>(relaxed = true)
    val mockDocumentSnapshot1 = mockk<DocumentSnapshot>(relaxed = true)
    val mockDocumentSnapshot2 = mockk<DocumentSnapshot>(relaxed = true)
    val mockFirebaseUser = mockk<FirebaseUser>(relaxed = true)
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
  }

  @Test
  fun fullAppNavigationTest() {
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("loginButton").assertExists().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    composeTestRule.onNodeWithTag("seeFriendsFab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("addContactScreen").assertExists()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule.onNodeWithTag("userList").assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }
    composeTestRule.onNodeWithTag("userList").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("contactScreen").assertExists()

    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("addContactScreen").assertExists()
    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    composeTestRule.onNodeWithTag("settingsFab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settingsScreen").assertExists()

    composeTestRule.onNodeWithTag("StorageAndDataOption").performClick()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule.onNodeWithTag("storageAndDataScreen").assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }
    composeTestRule.onNodeWithTag("storageAndDataScreen").assertExists()

    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settingsScreen").assertExists()

    composeTestRule.onNodeWithTag("goBackButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()
  }

  @Test
  fun testSendMessageEoE() {
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("loginButton").assertExists().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule.onNodeWithTag("userRow").assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }
    composeTestRule.onNodeWithTag("userRow").assertExists().performClick()

    composeTestRule.onNodeWithTag("sendMessageButton").assertExists().performClick()

    composeTestRule.onNodeWithTag("ChatInputTextBox").performTextInput("Hello, how are you?")
    composeTestRule.onNodeWithTag("SendButton").assertExists().performClick()

    composeTestRule
        .onNodeWithTag("ChatInputTextBox")
        .performTextInput("Do you want to meet at Rolex today at 10?")
    composeTestRule.onNodeWithTag("SendButton").assertExists().performClick()
  }
}
