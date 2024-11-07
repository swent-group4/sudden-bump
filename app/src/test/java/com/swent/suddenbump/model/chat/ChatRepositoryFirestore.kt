package com.swent.suddenbump.model.chat

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.swent.suddenbump.model.user.User
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer

class ChatRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockAuth: FirebaseAuth

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

    // Mock Firestore collections and documents
    `when`(mockFirestore.collection("messages")).thenReturn(mockMessagesCollection)
    `when`(mockFirestore.collection("chats")).thenReturn(mockChatsCollection)
    `when`(mockChatsCollection.document(anyString())).thenReturn(mockChatDocument)
    `when`(mockChatsCollection.document()).thenReturn(mockChatDocument)
    `when`(mockChatDocument.collection("messages")).thenReturn(mockMessagesSubCollection)
    `when`(mockMessagesSubCollection.document(anyString())).thenReturn(mockMessageDocument)

    // Initialize the repository AFTER setting up the mocks
    chatRepository = ChatRepositoryFirestore(mockFirestore)
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
    mockStatic(FirebaseAuth::class.java).use { firebaseAuthMockStatic ->
      // Arrange
      val userId = "user123"
      val currentUserId = "user456"
      val chatId = "chat789"
      val mockChatQuerySnapshot = mock(QuerySnapshot::class.java)
      val mockChatDocumentSnapshot = mock(DocumentSnapshot::class.java)
      val mockFirebaseUser = mock(FirebaseUser::class.java)

      // Mock FirebaseAuth static methods
      firebaseAuthMockStatic
          .`when`<FirebaseAuth> { FirebaseAuth.getInstance() }
          .thenReturn(mockAuth)

      // Mock FirebaseAuth current user
      `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)
      `when`(mockFirebaseUser.uid).thenReturn(currentUserId)

      // Mock Firestore query
      `when`(mockChatsCollection.whereArrayContains("participants", userId)).thenReturn(mockQuery)
      `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockChatQuerySnapshot))
      `when`(mockChatQuerySnapshot.documents).thenReturn(listOf(mockChatDocumentSnapshot))
      `when`(mockChatDocumentSnapshot.id).thenReturn(chatId)
      `when`(mockChatDocumentSnapshot.get("participants")).thenReturn(listOf(userId, currentUserId))

      // Act
      val resultChatId = chatRepository.getOrCreateChat(userId)

      // Assert
      assertEquals(chatId, resultChatId)
    }
  }

  @Test
  fun test_getOrCreateChat_newChat() = runBlocking {
    mockStatic(FirebaseAuth::class.java).use { firebaseAuthMockStatic ->
      // Arrange
      val userId = "user123"
      val currentUserId = "user456"
      val newChatId = "newChatId"
      val mockChatQuerySnapshot = mock(QuerySnapshot::class.java)
      val mockNewChatDocumentRef = mock(DocumentReference::class.java)
      val mockTaskVoid = Tasks.forResult<Void>(null)
      val mockFirebaseUser = mock(FirebaseUser::class.java)

      // Mock FirebaseAuth static methods
      firebaseAuthMockStatic
          .`when`<FirebaseAuth> { FirebaseAuth.getInstance() }
          .thenReturn(mockAuth)

      // Mock FirebaseAuth current user
      `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)
      `when`(mockFirebaseUser.uid).thenReturn(currentUserId)

      // Mock Firestore query returns empty list
      `when`(mockChatsCollection.whereArrayContains("participants", userId)).thenReturn(mockQuery)
      `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockChatQuerySnapshot))
      `when`(mockChatQuerySnapshot.documents).thenReturn(emptyList())

      // Mock creating new chat
      `when`(mockChatsCollection.document()).thenReturn(mockNewChatDocumentRef)
      `when`(mockNewChatDocumentRef.id).thenReturn(newChatId)
      `when`(mockNewChatDocumentRef.set(anyMap<String, Any>())).thenReturn(mockTaskVoid)

      // Act
      val resultChatId = chatRepository.getOrCreateChat(userId)

      // Assert
      assertEquals(newChatId, resultChatId)
    }
  }

  @Test
  fun test_getOrCreateChat_noCurrentUser() = runBlocking {
    mockStatic(FirebaseAuth::class.java).use { firebaseAuthMockStatic ->
      // Arrange
      val userId = "user123"

      // Mock FirebaseAuth static methods
      firebaseAuthMockStatic
          .`when`<FirebaseAuth> { FirebaseAuth.getInstance() }
          .thenReturn(mockAuth)

      // Mock FirebaseAuth current user is null
      `when`(mockAuth.currentUser).thenReturn(null)

      // Act
      val resultChatId = chatRepository.getOrCreateChat(userId)

      // Assert
      assertEquals("", resultChatId)
    }
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

  @Test
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
  }

  @Test
  fun test_getChatSummaries_success() = runBlocking {
    mockStatic(FirebaseAuth::class.java).use { firebaseAuthMockStatic ->
      // Arrange
      val currentUserId = "user456"
      val chatSummary1 =
          ChatSummary("chat1", "Hello", "user1", Timestamp.now(), 0, listOf("user1", currentUserId))
      val chatSummary2 =
          ChatSummary("chat2", "Hi", "user2", Timestamp.now(), 0, listOf("user2", currentUserId))
      val mockQuerySnapshot = mock(QuerySnapshot::class.java)
      val mockDocumentSnapshot1 = mock(DocumentSnapshot::class.java)
      val mockDocumentSnapshot2 = mock(DocumentSnapshot::class.java)
      val mockFirebaseUser = mock(FirebaseUser::class.java)

      // Mock FirebaseAuth static methods
      firebaseAuthMockStatic
          .`when`<FirebaseAuth> { FirebaseAuth.getInstance() }
          .thenReturn(mockAuth)

      // Mock FirebaseAuth current user
      `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)
      `when`(mockFirebaseUser.uid).thenReturn(currentUserId)

      // Mock Firestore query
      `when`(mockChatsCollection.whereArrayContains("participants", currentUserId))
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
      `when`(mockDocumentSnapshot1.toObject(ChatSummary::class.java)).thenReturn(chatSummary1)
      `when`(mockDocumentSnapshot2.toObject(ChatSummary::class.java)).thenReturn(chatSummary2)

      // Act
      val chatSummariesFlow = chatRepository.getChatSummaries()
      val chatSummaries = chatSummariesFlow.first()

      // Assert
      assertEquals(2, chatSummaries.size)
      assertEquals(chatSummary1, chatSummaries[0])
      assertEquals(chatSummary2, chatSummaries[1])
    }
  }

  @Test
  fun test_getChatSummaries_noCurrentUser() = runBlocking {
    mockStatic(FirebaseAuth::class.java).use { firebaseAuthMockStatic ->
      // Arrange
      // Mock FirebaseAuth static methods
      firebaseAuthMockStatic
          .`when`<FirebaseAuth> { FirebaseAuth.getInstance() }
          .thenReturn(mockAuth)

      // Mock FirebaseAuth current user is null
      `when`(mockAuth.currentUser).thenReturn(null)

      // Act & Assert
      try {
        chatRepository.getChatSummaries().collect()
        fail("Expected exception was not thrown")
      } catch (e: Exception) {
        assertTrue(e.cause is IllegalStateException)
        assertEquals("User not logged in", e.cause?.message)
      }
    }
  }

  @Test
  fun test_getChatSummaries_failure() = runBlocking {
    mockStatic(FirebaseAuth::class.java).use { firebaseAuthMockStatic ->
      // Arrange
      val currentUserId = "user456"
      val exception = FirebaseFirestoreException("Error", FirebaseFirestoreException.Code.ABORTED)
      val mockFirebaseUser = mock(FirebaseUser::class.java)

      // Mock FirebaseAuth static methods
      firebaseAuthMockStatic
          .`when`<FirebaseAuth> { FirebaseAuth.getInstance() }
          .thenReturn(mockAuth)
      // Mock FirebaseAuth current user
      `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)
      `when`(mockFirebaseUser.uid).thenReturn(currentUserId)

      // Mock Firestore query
      `when`(mockChatsCollection.whereArrayContains("participants", currentUserId))
          .thenReturn(mockQuery)
      doAnswer { invocation ->
            val listener = invocation.getArgument<EventListener<QuerySnapshot>>(0)
            listener.onEvent(null, exception)
            mockListenerRegistration
          }
          .`when`(mockQuery)
          .addSnapshotListener(any<EventListener<QuerySnapshot>>())

      // Act & Assert
      try {
        chatRepository.getChatSummaries().collect()
        fail("Expected exception was not thrown")
      } catch (e: Exception) {
        assertTrue(e.cause is FirebaseFirestoreException)
        assertEquals("Error", e.cause?.message)
      }
    }
  }

  @Test
  fun test_getUserAccount_success() = runBlocking {
    // Arrange
    val uid = "user123"
    val user = User(uid, "Test", "User", "+1234567890", null, "test@example.com")
    val mockUserDocumentRef = mock(DocumentReference::class.java)
    val mockUserDocumentSnapshot = mock(DocumentSnapshot::class.java)
    val mockTaskDocumentSnapshot = mock(Task::class.java) as Task<DocumentSnapshot>

    // Mock Firestore
    val mockUsersCollection = mock(CollectionReference::class.java)
    `when`(mockFirestore.collection("Users")).thenReturn(mockUsersCollection)
    `when`(mockUsersCollection.document(uid)).thenReturn(mockUserDocumentRef)
    `when`(mockUserDocumentRef.get()).thenReturn(mockTaskDocumentSnapshot)
    `when`(mockTaskDocumentSnapshot.isSuccessful).thenReturn(true)
    `when`(mockTaskDocumentSnapshot.result).thenReturn(mockUserDocumentSnapshot)
    `when`(mockUserDocumentSnapshot.toObject(User::class.java)).thenReturn(user)

    // Act
    val resultUser = chatRepository.getUserAccount(uid)

    // Assert
    assertNotNull(resultUser)
    assertEquals(user, resultUser)
  }

  @Test
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
  }
}
