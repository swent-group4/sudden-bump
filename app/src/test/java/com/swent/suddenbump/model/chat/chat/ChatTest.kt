package com.swent.suddenbump.model.chat.chat

import com.google.firebase.Timestamp
import com.swent.suddenbump.model.chat.Chat
import com.swent.suddenbump.model.chat.Message
import org.junit.Assert.assertEquals
import org.junit.Test

class ChatTest {
  @Test
  fun testChatDefaultValues() {
    val chat = Chat()
    assertEquals("", chat.chatId)
    assertEquals(null, chat.lastMessage)
    assertEquals(listOf<String>(), chat.participants)
    assertEquals(mapOf<String, Int>(), chat.unreadCountByUser)
  }

  @Test
  fun testChatInitialization() {
    val timestamp = Timestamp.now()
    val chat =
        Chat(
            chatId = "chat123",
            lastMessage = Message(content = "Hello"),
            lastMessageTimestamp = timestamp,
            participants = listOf("user1", "user2"),
            unreadCountByUser = mapOf("user1" to 2))
    assertEquals("chat123", chat.chatId)
    assertEquals("Hello", chat.lastMessage?.content)
    assertEquals(listOf("user1", "user2"), chat.participants)
    assertEquals(mapOf("user1" to 2), chat.unreadCountByUser)
  }
}
