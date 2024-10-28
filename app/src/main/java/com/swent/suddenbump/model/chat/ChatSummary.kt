package com.swent.suddenbump.model.chat

import com.google.firebase.Timestamp

data class ChatSummary(
    val lastMessage: String = "",
    val lastMessageSender: String = "",
    val lastMessageTimestamp: Timestamp? = null,
    val unreadCount: Int = 0,
    val participants: List<String> = emptyList(),
    var otherUserName: String = "",
){
    val sender: String
        get() = otherUserName
    val content: String
        get() = lastMessage
    val date: String
        get() = lastMessageTimestamp?.toDate()?.toCustomFormat()?: ""
}