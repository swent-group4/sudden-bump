package com.swent.suddenbump.model.chat

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.swent.suddenbump.model.user.User

data class ChatSummary(
    @DocumentId val id: String = "",
    val lastMessage: String = "",
    val lastMessageSender: String = "",
    val lastMessageTimestamp: Timestamp? = null,
    val unreadCount: Int = 0,
    val participants: List<String> = emptyList(),
    var otherUser: User? = null,
) {
  val sender: String
    get() =
        "${otherUser?.firstName?.replaceFirstChar { it.uppercase() }} ${otherUser?.lastName?.replaceFirstChar { it.uppercase() }}"

  val content: String
    get() = lastMessage

  val date: String
    get() = lastMessageTimestamp?.toDate()?.toCustomFormat() ?: ""
}
