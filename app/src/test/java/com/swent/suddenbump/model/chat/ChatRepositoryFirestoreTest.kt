package com.swent.suddenbump.model.chat

import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.swent.suddenbump.model.user.User
import java.util.Date
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer

class ChatRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockAuth: FirebaseAuth
  @Mock private lateinit var mockAuthUser: FirebaseUser
  private lateinit var firebaseAuthMockStatic: MockedStatic<FirebaseAuth>
  @Mock private lateinit var mockUsersCollection: CollectionReference
  @Mock private lateinit var mockUserDocument: DocumentReference
  // Firestore collections and documents
  @Mock private lateinit var mockMessagesCollection: CollectionReference
  @Mock private lateinit var mockChatsCollection: CollectionReference
  @Mock private lateinit var mockChatDocument: DocumentReference
  @Mock private lateinit var mockMessagesSubCollection: CollectionReference
  @Mock private lateinit var mockMessageDocument: DocumentReference

  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockQuery: Query
  @Mock private lateinit var mockListenerRegistration: ListenerRegistration

  // User-related mocks
  @Mock private lateinit var mockUser: User

  private lateinit var chatRepository: ChatRepositoryFirestore

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    // Initialize mocks
    mockAuth = mock(FirebaseAuth::class.java)
    mockAuthUser = mock(FirebaseUser::class.java)
    mockFirestore = mock(FirebaseFirestore::class.java)
    mockChatsCollection = mock(CollectionReference::class.java)
    mockQuery = mock(Query::class.java)

    // Mock Firestore collections and documents
    `when`(mockFirestore.collection("messages")).thenReturn(mockMessagesCollection)
    `when`(mockFirestore.collection("chats")).thenReturn(mockChatsCollection)
    `when`(mockChatsCollection.document(anyString())).thenReturn(mockChatDocument)
    `when`(mockChatsCollection.document()).thenReturn(mockChatDocument)
    `when`(mockChatDocument.collection("messages")).thenReturn(mockMessagesSubCollection)
    `when`(mockMessagesSubCollection.document(anyString())).thenReturn(mockMessageDocument)
    `when`(mockFirestore.collection("Users")).thenReturn(mockUsersCollection)
    // Mock FirebaseAuth current user
    `when`(mockAuth.currentUser).thenReturn(mockAuthUser)
    `when`(mockAuthUser.uid).thenReturn("user456") // Set default current user ID

    // Initialize the repository with mocked FirebaseAuth
    chatRepository = ChatRepositoryFirestore(mockFirestore)

    // Mock FirebaseAuth.getInstance()
    firebaseAuthMockStatic = mockStatic(FirebaseAuth::class.java)
    firebaseAuthMockStatic.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockAuth)
    // Mock FirebaseAuth current user
    `when`(mockAuth.currentUser).thenReturn(mockAuthUser)
    `when`(mockAuthUser.uid).thenReturn("user456") // Set default current user ID

    // Set up default behavior for mocks
    `when`(mockFirestore.collection("chats")).thenReturn(mockChatsCollection)
  }

  @After
  fun tearDown() {
    firebaseAuthMockStatic.close()
  }

  @Test
  fun test_fetchMessages_success() = runBlocking {
    // Arrange
    val mockQuerySnapshot = mock(QuerySnapshot::class.java)
    val mockDocumentSnapshot1 = mock(DocumentSnapshot::class.java)
    val mockDocumentSnapshot2 = mock(DocumentSnapshot::class.java)
    val message1 = Message("msg1", "user1", "Hello", Timestamp.now(), listOf("user1"))
    val message2 = Message("msg2", "user2", "Hi", Timestamp.now(), listOf("user2"))

    // Mock Firestore query
    `when`(mockMessagesCollection.orderBy("timestamp", Query.Direction.ASCENDING))
        .thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot1, mockDocumentSnapshot2))
    `when`(mockDocumentSnapshot1.toObject(Message::class.java)).thenReturn(message1)
    `when`(mockDocumentSnapshot2.toObject(Message::class.java)).thenReturn(message2)

    // Act
    val result = chatRepository.fetchMessages()

    // Assert
    assertEquals(2, result.size)
    assertEquals(message1, result[0])
    assertEquals(message2, result[1])
  }

  @Test
  fun test_fetchMessages_failure() = runBlocking {
    // Arrange
    val exception = Exception("Firestore error")
    `when`(mockMessagesCollection.orderBy("timestamp", Query.Direction.ASCENDING))
        .thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(Tasks.forException(exception))

    // Act
    val result = chatRepository.fetchMessages()

    // Assert
    assertTrue(result.isEmpty())
  }

  @Test
  fun test_getOrCreateChat_existingChat() = runBlocking {
    // Arrange
    val friendId = "user123"
    val userId = "user456"
    val chatId = "chat789"
    val mockChatQuerySnapshot = mock(QuerySnapshot::class.java)
    val mockChatDocumentSnapshot = mock(DocumentSnapshot::class.java)

    // Mock Firestore query
    `when`(mockChatsCollection.whereArrayContains("participants", userId)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockChatQuerySnapshot))
    `when`(mockChatQuerySnapshot.documents).thenReturn(listOf(mockChatDocumentSnapshot))
    `when`(mockChatDocumentSnapshot.id).thenReturn(chatId)
    `when`(mockChatDocumentSnapshot.get("participants")).thenReturn(listOf(userId, friendId))

    // Act
    val resultChatId = chatRepository.getOrCreateChat(userId, friendId)

    // Assert
    assertEquals(chatId, resultChatId)
  }

  @Test
  fun test_getOrCreateChat_newChat() = runBlocking {
    // Arrange
    val userId = "user123"
    val currentUserId = "user456"
    val newChatId = "newChatId"
    val mockChatQuerySnapshot = mock(QuerySnapshot::class.java)
    val mockNewChatDocumentRef = mock(DocumentReference::class.java)
    val mockTaskVoid = Tasks.forResult<Void>(null)

    // Mock FirebaseAuth current user (if different from default)
    `when`(mockAuth.currentUser).thenReturn(mockAuthUser)
    `when`(mockAuthUser.uid).thenReturn(currentUserId)

    // Mock Firestore query returns empty list
    `when`(mockChatsCollection.whereArrayContains("participants", userId)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockChatQuerySnapshot))
    `when`(mockChatQuerySnapshot.documents).thenReturn(emptyList())

    // Mock creating new chat
    `when`(mockChatsCollection.document()).thenReturn(mockNewChatDocumentRef)
    `when`(mockNewChatDocumentRef.id).thenReturn(newChatId)
    `when`(mockNewChatDocumentRef.set(any<Map<String, Any>>())).thenReturn(mockTaskVoid)

    // Act
    val resultChatId = chatRepository.getOrCreateChat(userId, currentUserId)

    // Assert
    assertEquals(newChatId, resultChatId)
  }

  @Test
  fun test_getOrCreateChat_noCurrentUser() = runBlocking {
    // Arrange
    val otherUserId = "otherUser123"

    // Mock currentUser to return null
    `when`(mockAuth.currentUser).thenReturn(null)

    // Act
    val chatId = chatRepository.getOrCreateChat(otherUserId, "")

    // Assert
    assertEquals("", chatId)
  }

  @Test
  fun test_getMessages_success() = runBlocking {
    // Arrange
    val chatId = "chat123"
    val message1 = Message("msg1", "user1", "Hello", Timestamp.now(), listOf("user1"))
    val message2 = Message("msg2", "user2", "Hi", Timestamp.now(), listOf("user2"))
    val mockQuerySnapshot = mock(QuerySnapshot::class.java)
    val mockDocumentSnapshot1 = mock(DocumentSnapshot::class.java)
    val mockDocumentSnapshot2 = mock(DocumentSnapshot::class.java)

    // Mock Firestore query
    `when`(mockFirestore.collection("chats")).thenReturn(mockChatsCollection)
    `when`(mockChatsCollection.document(chatId)).thenReturn(mockChatDocument)
    `when`(mockChatDocument.collection("messages")).thenReturn(mockMessagesSubCollection)
    `when`(mockMessagesSubCollection.orderBy("timestamp", Query.Direction.DESCENDING))
        .thenReturn(mockQuery)

    doAnswer { invocation ->
          val listener = invocation.getArgument<EventListener<QuerySnapshot>>(0)
          listener.onEvent(mockQuerySnapshot, null)
          mockListenerRegistration
        }
        .`when`(mockQuery)
        .addSnapshotListener(any<EventListener<QuerySnapshot>>())

    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot1, mockDocumentSnapshot2))
    `when`(mockDocumentSnapshot1.toObject(Message::class.java)).thenReturn(message1)
    `when`(mockDocumentSnapshot2.toObject(Message::class.java)).thenReturn(message2)

    // Act
    val messagesFlow = chatRepository.getMessages(chatId)
    val messages = messagesFlow.first()

    // Assert
    assertEquals(2, messages.size)
    assertEquals(message1, messages[0])
    assertEquals(message2, messages[1])
  }

  //  @Test
  //  fun test_sendMessage_success() {
  //    runBlocking {
  //      // Arrange
  //      val chatId = "chat123"
  //      val messageContent = "Hello, World!"
  //      val username = "Test User"
  //      val senderId = "user123"
  //      val senderName = "Sender"
  //
  //      val user = User(senderId, "Test", "User", "+123456789", null, "user@test.com", null)
  //
  //      // Mock currentUser
  //      `when`(mockAuthUser.uid).thenReturn(senderId)
  //      `when`(mockAuthUser.displayName).thenReturn(senderName)
  //
  //      // Mock Firestore calls
  //      `when`(mockFirestore.collection("chats")).thenReturn(mockChatsCollection)
  //      `when`(mockChatsCollection.document(chatId)).thenReturn(mockChatDocument)
  //      `when`(mockChatDocument.collection("messages")).thenReturn(mockMessagesSubCollection)
  //      `when`(mockMessagesSubCollection.add(any(Message::class.java)))
  //          .thenReturn(Tasks.forResult(mockMessageDocument))
  //      `when`(
  //              mockChatDocument.update(
  //                  mapOf(
  //                      "lastMessage" to messageContent,
  //                      "lastMessageTimestamp" to FieldValue.serverTimestamp(),
  //                      "lastMessageSender" to senderName,
  //                      "otherUserName" to username)))
  //          .thenReturn(Tasks.forResult(null))
  //
  //      // Act
  //      chatRepository.sendMessage(chatId, messageContent, user)
  //
  //      // Assert
  //      verify(mockMessagesSubCollection).add(any(Message::class.java))
  //      verify(mockChatDocument)
  //          .update(
  //              mapOf(
  //                  "lastMessage" to messageContent,
  //                  "lastMessageTimestamp" to FieldValue.serverTimestamp(),
  //                  "lastMessageSender" to senderName,
  //                  "otherUserName" to username))
  //    }
  //  }

  @Test
  fun test_getChatSummaries_success() {
    runBlocking {
      // Arrange
      val currentUserId = "user123"
      val timestamp1 = Timestamp(Date(2000L)) // Later timestamp
      val timestamp2 = Timestamp(Date(1000L)) // Earlier timestamp

      val chatSummary1 =
          ChatSummary("chat1", "Hello", "user1", timestamp1, 1, listOf(currentUserId, "user2"))
      val chatSummary2 =
          ChatSummary("chat2", "Hi", "user2", timestamp2, 0, listOf(currentUserId, "user3"))

      // Mock FirebaseAuth
      val mockAuthUser = mock(FirebaseUser::class.java)
      `when`(mockAuth.currentUser).thenReturn(mockAuthUser)
      `when`(mockAuthUser.uid).thenReturn(currentUserId)

      // Mock Query returned by whereArrayContains
      val mockQuery = mock(Query::class.java)
      `when`(mockChatsCollection.whereArrayContains("participants", currentUserId))
          .thenReturn(mockQuery)

      // Mock QuerySnapshot and Documents
      val mockChatSnapshot = mock(QuerySnapshot::class.java)
      val mockDocumentSnapshot1 = mock(DocumentSnapshot::class.java)
      val mockDocumentSnapshot2 = mock(DocumentSnapshot::class.java)
      `when`(mockChatSnapshot.documents)
          .thenReturn(listOf(mockDocumentSnapshot1, mockDocumentSnapshot2))
      `when`(mockDocumentSnapshot1.toObject(ChatSummary::class.java)).thenReturn(chatSummary1)
      `when`(mockDocumentSnapshot2.toObject(ChatSummary::class.java)).thenReturn(chatSummary2)

      // Mock addSnapshotListener on the Query
      doAnswer { invocation ->
            val listener = invocation.getArgument<EventListener<QuerySnapshot>>(0)
            listener.onEvent(mockChatSnapshot, null)
            mockListenerRegistration
          }
          .`when`(mockQuery)
          .addSnapshotListener(any<EventListener<QuerySnapshot>>())

      // Act
      val chatSummariesFlow = chatRepository.getChatSummaries(currentUserId)
      val chatSummaries = chatSummariesFlow.first()

      // Assert
      assertEquals(2, chatSummaries.size)
      assertEquals(chatSummary1, chatSummaries[0])
      assertEquals(chatSummary2, chatSummaries[1])
    }
  }

  @Test
  fun test_getChatSummaries_failure() = runBlocking {
    // Arrange
    val currentUserId = "user123"
    val expectedException =
        FirebaseFirestoreException("Firestore error", FirebaseFirestoreException.Code.ABORTED)

    // Mock currentUser to return a valid user
    `when`(mockAuth.currentUser).thenReturn(mockAuthUser)
    `when`(mockAuthUser.uid).thenReturn(currentUserId)

    // Mock the query returned by whereArrayContains
    `when`(mockChatsCollection.whereArrayContains("participants", currentUserId))
        .thenReturn(mockQuery)

    // Mock addSnapshotListener to simulate an error
    doAnswer { invocation ->
          val listener = invocation.getArgument<EventListener<QuerySnapshot>>(0)
          listener.onEvent(null, expectedException)
          mockListenerRegistration
        }
        .`when`(mockQuery)
        .addSnapshotListener(any<EventListener<QuerySnapshot>>())

    // Act & Assert
    try {
      val chatSummariesFlow = chatRepository.getChatSummaries(currentUserId)
      // Collect the flow
      chatSummariesFlow.collect { summaries ->
        // We should not receive any summaries
        fail("Should not receive any summaries when there is a Firestore error")
      }
      fail("Expected exception not thrown")
    } catch (e: FirebaseFirestoreException) {
      // Assert that the exception is the expected one
      assertEquals(expectedException, e)
      assertEquals("Firestore error", e.message)
      assertEquals(FirebaseFirestoreException.Code.ABORTED, e.code)
    }
  }
  /*@Test
  fun test_getUserAccount_success() = runBlocking {
    // Arrange
    val uid = "user123"
    val user = User(uid, "John", "Doe", "+123456789", null, "john.doe@example.com", GeoLocation(0.0, 0.0))

    // Mock 'collection("Users")' to return 'mockUsersCollection'
    `when`(mockFirestore.collection("Users")).thenReturn(mockUsersCollection)

    // Mock 'document(uid)' to return 'mockUserDocument'
    `when`(mockUsersCollection.document(uid)).thenReturn(mockUserDocument)

    // Create a Task that completes immediately with 'mockDocumentSnapshot'
    val mockTask = Tasks.forResult(mockDocumentSnapshot) as Task<DocumentSnapshot>

    // Mock 'get()' to return the completed Task
    `when`(mockUserDocument.get()).thenReturn(mockTask)

    // Mock 'toObject' to return our 'user' object
    `when`(mockDocumentSnapshot.toObject(User::class.java)).thenReturn(user)

    // Act
    val result = chatRepository.getUserAccount(uid)

    // Assert
    assertNotNull(result)
    assertEquals(user, result)
  }*/

  /*@Test
  fun test_getMessages_failure() = runBlocking {
    // Arrange
    val chatId = "chat123"
    val exception = FirebaseFirestoreException("Error", FirebaseFirestoreException.Code.ABORTED)

    // Mock Firestore query
    `when`(mockFirestore.collection("chats")).thenReturn(mockChatsCollection)
    `when`(mockChatsCollection.document(chatId)).thenReturn(mockChatDocument)
    `when`(mockChatDocument.collection("messages")).thenReturn(mockMessagesSubCollection)
    `when`(mockMessagesSubCollection.orderBy("timestamp", Query.Direction.DESCENDING))
        .thenReturn(mockQuery)

    doAnswer { invocation ->
          val listener = invocation.getArgument<EventListener<QuerySnapshot>>(0)
          listener.onEvent(null, exception)
          mockListenerRegistration
        }
        .`when`(mockQuery)
        .addSnapshotListener(any<EventListener<QuerySnapshot>>())

    // Act & Assert
    val messagesFlow = chatRepository.getMessages(chatId)
    try {
      messagesFlow.collect()

      fail("Expected exception was not thrown")
    } catch (e: Exception) {
      assertTrue(e.cause is FirebaseFirestoreException)
      assertEquals("Error", e.cause?.message)
    }
  }*/

  /*@Test
  fun test_getUserAccount_failure() = runBlocking {
    // Arrange
    val uid = "user123"
    val mockUserDocumentRef = mock(DocumentReference::class.java)
    val exception = Exception("Error")
    val mockTaskDocumentSnapshot = mock(Task::class.java) as Task<DocumentSnapshot>

    // Mock Firestore
    val mockUsersCollection = mock(CollectionReference::class.java)
    `when`(mockFirestore.collection("Users")).thenReturn(mockUsersCollection)
    `when`(mockUsersCollection.document(uid)).thenReturn(mockUserDocumentRef)
    `when`(mockUserDocumentRef.get()).thenReturn(mockTaskDocumentSnapshot)
    `when`(mockTaskDocumentSnapshot.isSuccessful).thenReturn(false)
    `when`(mockTaskDocumentSnapshot.exception).thenReturn(exception)

    // Act
    val resultUser = chatRepository.getUserAccount(uid)

    // Assert
    assertNull(resultUser)
  }*/
}
