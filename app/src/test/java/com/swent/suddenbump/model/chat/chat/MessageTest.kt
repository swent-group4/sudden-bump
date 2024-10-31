package com.swent.suddenbump.model.chat.chat

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.swent.suddenbump.model.chat.Message
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class MessageTest {

  private lateinit var mockAuth: FirebaseAuth
  private lateinit var mockUser: FirebaseUser

  @Before
  fun setUp() {
    // Mock FirebaseAuth and FirebaseUser
    mockAuth = Mockito.mock(FirebaseAuth::class.java)
    mockUser = Mockito.mock(FirebaseUser::class.java)

    // Set the FirebaseAuth mock to return the mockUser when currentUser is called
    `when`(mockAuth.currentUser).thenReturn(mockUser)
  }

  @Test
  fun `isOwner returns true when senderId matches current user ID`() {
    // Arrange
    val currentUserUid = "testUserId"
    `when`(mockUser.uid).thenReturn(currentUserUid) // Set UID for the mock user

    val message =
        Message(
            messageId = "1",
            senderId = currentUserUid,
            content = "Hello, world!",
            timestamp = Timestamp.now(),
            isReadBy = listOf("user1", "user2"),
            auth = mockAuth // Inject mockAuth here
            )

    // Act & Assert
    assertTrue(message.isOwner)
  }

  @Test
  fun `isOwner returns false when senderId does not match current user ID`() {
    // Arrange
    val currentUserUid = "testUserId"
    `when`(mockUser.uid).thenReturn(currentUserUid) // Set UID for the mock user

    val message =
        Message(
            messageId = "2",
            senderId = "anotherUserId",
            content = "Hello, world!",
            timestamp = Timestamp.now(),
            isReadBy = listOf("user1", "user2"),
            auth = mockAuth // Inject mockAuth here
            )

    // Act & Assert
    assertFalse(message.isOwner)
  }
}
