package com.swent.suddenbump.model.chat.chat

import com.google.firebase.Timestamp
import com.swent.suddenbump.model.chat.ChatSummary
import com.swent.suddenbump.model.user.User
import org.junit.Test
import org.junit.Assert.*

class ChatSummaryTest {
    @Test
    fun testSenderFormatting() {
        // Arrange: create a user with first and last names
        val user = User(firstName = "John", lastName = "Doe")

        // Act: create a ChatSummary with this user
        val chatSummary = ChatSummary(otherUser = user)

        // Assert: verify that the sender format is as expected
        assertEquals("John Doe", chatSummary.sender)
    }

    @Test
    fun testDateFormatting() {
        val timestamp = Timestamp.now()
        val chatSummary = ChatSummary(lastMessageTimestamp = timestamp)
        assertTrue(chatSummary.date.isNotEmpty()) // Ensures non-empty date string
    }
}
