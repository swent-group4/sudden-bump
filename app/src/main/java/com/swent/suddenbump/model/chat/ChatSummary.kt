package com.swent.suddenbump.model.chat

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.swent.suddenbump.model.user.User

data class ChatSummary(
    @DocumentId val id: String = "",
    val lastMessage: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageTimestamp: Timestamp? = null,
    val unreadCount: Int = 0,
    val participants: List<String> = emptyList(),
) {
  val sender: String
    get() = participants.joinToString(", ")

  val content: String
    get() = lastMessage

  val date: String
    get() = lastMessageTimestamp?.toDate()?.toCustomFormat() ?: ""
}

/** Helper function to get real names */
fun convertParticipantsUidToDisplay(
    chatSummary: ChatSummary,
    currentUser: User,
    friendsList: List<User>
): String {
  return chatSummary.participants
      .filter { stringParticipant -> currentUser.uid != stringParticipant }
      .map { it2 ->
        val correctUser = friendsList.first { it3 -> it3.uid == it2 }
        "${correctUser.firstName} ${correctUser.lastName}"
      }
      .joinToString(", ")
}

/** Helper function to get last sender name */
fun convertLastSenderUidToDisplay(
    chatSummary: ChatSummary,
    currentUser: User,
    friendsList: List<User>
): String {
  return if (chatSummary.lastMessageSenderId == currentUser.uid) "You"
  else
      friendsList
          .first { it.uid == chatSummary.lastMessageSenderId }
          .let { "${it.firstName} ${it.lastName}" }
}
