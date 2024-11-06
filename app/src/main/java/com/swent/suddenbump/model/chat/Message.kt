package com.swent.suddenbump.model.chat

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId

data class Message(
    @DocumentId val messageId: String = "", // Unique ID for the message
    val senderId: String = "", // ID of the user who sent the message
    val content: String = "", // The text content of the message
    val timestamp: Timestamp = Timestamp.now(), // Timestamp when the message was sent
    val isReadBy: List<String> = emptyList() // List of user IDs who have read this message
) {
  val isOwner: Boolean
    get() = senderId == FirebaseAuth.getInstance().currentUser?.uid
}
