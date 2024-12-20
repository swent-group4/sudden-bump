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
        var correctUser = friendsList.firstOrNull { it3 -> it3.uid == it2 }
        if (correctUser == null) {
          correctUser = User.UnknownUser
        }
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
  else {
    var correctUser = friendsList.firstOrNull() { it.uid == chatSummary.lastMessageSenderId }
    if (correctUser == null) {
      correctUser = User.UnknownUser
    }
    "${correctUser.firstName} ${correctUser.lastName}"
  }
}

/** Helper function to get first non-current user in participants list from uid */
fun convertFirstParticipantToUser(chatSummary: ChatSummary, friendsList: List<User>): User {
  val correctUserUid = friendsList.map { it.uid }.firstOrNull { it in chatSummary.participants }
  var correctUser = User.UnknownUser
  if (correctUserUid != null) {
    correctUser = friendsList.first { it.uid == correctUserUid }
  }
  return correctUser
}
