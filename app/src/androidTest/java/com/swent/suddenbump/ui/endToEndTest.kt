package com.swent.suddenbump.ui

import android.location.Location
import android.util.Log
import androidx.compose.ui.test.assertTextContains
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
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.swent.suddenbump.MainActivity
import com.swent.suddenbump.model.chat.ChatRepositoryFirestore
import com.swent.suddenbump.model.chat.Message
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

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
      // Initialize MockK mocks for FirebaseAuth and FirebaseFirestore
      mockFirestore = mockk(relaxed = true)
      mockAuth = mockk(relaxed = true)
      chatRepository = mockk(relaxed = true)
      mockQuery = mockk(relaxed = true)
      mockFirebaseUser = mockk(relaxed = true)
      mockkStatic(FirebaseAuth::class)


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

      // Initialize Firestore collections and documents with MockK
      mockMessagesCollection = mockk(relaxed = true)
      mockMessageDocument = mockk(relaxed = true)
      val mockChatsCollection = mockk<CollectionReference>(relaxed = true)
      val mockChatDocument = mockk<DocumentReference>(relaxed = true)
      val mockMessagesSubCollection = mockk<CollectionReference>(relaxed = true)


      every { FirebaseAuth.getInstance() } returns mockAuth

      // Mock FirebaseAuth current user
      every { mockAuth.currentUser } returns mockFirebaseUser
      every { mockFirebaseUser.uid } returns currentUserId

// Mock Firestore query
      every { mockChatsCollection.whereArrayContains("participants", friendId) } returns mockQuery
      every { mockQuery.get() } returns Tasks.forResult(mockChatQuerySnapshot)
      every { mockChatQuerySnapshot.documents } returns listOf(mockChatDocumentSnapshot)
      every { mockChatDocumentSnapshot.id } returns chatId
      every { mockChatDocumentSnapshot.get("participants") } returns listOf(friendId, currentUserId)


      // Define mock behavior for Firestore collections and documents
      every { mockFirestore.collection("chats") } returns mockChatsCollection
      every { mockChatsCollection.document(any<String>()) } returns mockChatDocument
      every { mockChatDocument.collection("messages") } returns mockMessagesSubCollection
      every { mockMessagesSubCollection.orderBy("timestamp", Query.Direction.DESCENDING) } returns mockQuery

      // Mock Firestore query behavior using MockK
      every { mockQuery.get() } returns Tasks.forResult(mockQuerySnapshot)
      every { mockQuerySnapshot.documents } returns listOf(mockDocumentSnapshot1, mockDocumentSnapshot2)
      every { mockDocumentSnapshot1.toObject(Message::class.java) } returns message1
      every { mockDocumentSnapshot2.toObject(Message::class.java) } returns message2

      // Mock Firestore query
      every { mockMessagesCollection.orderBy("timestamp", Query.Direction.ASCENDING) } returns mockQuery
      every { mockQuery.get() } returns Tasks.forResult(mockQuerySnapshot)
      every { mockQuerySnapshot.documents } returns listOf(mockDocumentSnapshot1, mockDocumentSnapshot2)
      every { mockDocumentSnapshot1.toObject(Message::class.java) } returns message1
      every { mockDocumentSnapshot2.toObject(Message::class.java) } returns message2



  }

  @Test
  fun fullAppNavigationTest() {
    // Wait for the app to load
    composeTestRule.waitForIdle()

    // Step 1: Simulate user interaction for authentication
    composeTestRule.onNodeWithTag("loginButton").assertExists().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    // Step 2: Navigate to Friends List
    composeTestRule.onNodeWithTag("seeFriendsFab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("friendsListScreen").assertExists()

    // Step 3: Navigate to Add Contact screen
    composeTestRule.onNodeWithTag("addContactButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("addContactScreen").assertExists()

    // Step 4: Navigate to Contact screen
    composeTestRule.onNodeWithTag("userList").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("contactScreen").assertExists()

    // Step 5: Navigate back to Overview
    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("addContactScreen").assertExists()
    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("friendsListScreen").assertExists()
    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    // Step 6: Navigate to Settings screen
    composeTestRule.onNodeWithTag("settingsFab").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settingsScreen").assertExists()

    // Step 7: Navigate back to Overview
    composeTestRule.onNodeWithTag("customGoBackButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("overviewScreen").assertExists()

    // Step 8: Navigate to Map screen
    composeTestRule.onNodeWithTag("Map").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("mapView").assertExists()

    // Step 9: Navigate to Messages screen
    composeTestRule.onNodeWithTag("Messages").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("messages_list").assertExists()
  }

  @Test
  fun testSendMessageEoE() {

      // Wait for the app to load
      composeTestRule.waitForIdle()

      // Step 1: Simulate user interaction for authentication
      composeTestRule.onNodeWithTag("loginButton").assertExists().performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("overviewScreen").assertExists()

      composeTestRule.onNodeWithTag("1").assertExists().performClick()

      composeTestRule.onNodeWithTag("sendMessageButton").assertExists().performClick()

      composeTestRule.onNodeWithTag("ChatInputTextBox").performTextInput("Hello, how are you?")

      composeTestRule.onNodeWithTag("SendButton").assertExists().performClick()

      composeTestRule.onNodeWithTag("ChatInputTextBox").performTextInput("Do you want to meet at Rolex today at 10?")

      composeTestRule.onNodeWithTag("SendButton").assertExists().performClick()


  }
}
