package com.swent.suddenbump.model.chat

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Chat(
    @DocumentId
    val chatId: String = "",                   // Unique chat ID
    val lastMessage: Message? = null,          // Last message in the chat
    val lastMessageTimestamp: Timestamp = Timestamp.now(),          // Last message in the chat
    val participants: List<String> = listOf(),        // List of user IDs involved in the chat
    val unreadCountByUser: Map<String, Int> = mapOf()  // Map to track unread count per user
)