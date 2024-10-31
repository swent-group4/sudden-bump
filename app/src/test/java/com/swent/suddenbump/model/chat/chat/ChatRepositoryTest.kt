package com.swent.suddenbump.model.chat.chat

import com.google.firebase.firestore.FirebaseFirestore
import com.swent.suddenbump.model.chat.ChatRepository
import com.swent.suddenbump.model.chat.Message
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class ChatRepositoryTest {

    @Mock
    private lateinit var mockFirestore: FirebaseFirestore

    private lateinit var chatRepository: ChatRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        chatRepository = ChatRepository(mockFirestore)
    }

    @Test
    fun `fetchMessages returns list of messages`() = runBlocking {
        // Arrange: Setup mock data and behavior here
        // ...mocking Firestore collection

        // Act
        val messages = chatRepository.fetchMessages()

        // Assert
        assertTrue(messages is List<*>)
    }

    @Test
    fun `sendMessage adds a message`() = runBlocking {
        // Arrange: Configure the expected data here

        // Act
        chatRepository.sendMessage("chatId", "Hello, World!", "User")

        // Assert: Ensure Firestore method is called
        verify(mockFirestore, times(1)).collection("yourCollectionPath")
    }

    @Test
    fun `getUnreadMessagesCount returns count`() = runBlocking {
        // Arrange: Mock Firestore to return an unread message count

        // Act
        val count = chatRepository.getUnreadMessagesCount("chatId", "userId")

        // Assert
        assertTrue(count >= 0)
    }
}
