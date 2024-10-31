package com.swent.suddenbump.model.chat

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId

data class Message(
    @DocumentId
    val messageId: String = "",
    val senderId: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isReadBy: List<String> = emptyList(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance() // Injected for testability
) {
    val isOwner: Boolean
        get() = senderId == auth.currentUser?.uid
}
