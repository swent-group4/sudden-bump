package com.swent.suddenbump.model.chat

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.swent.suddenbump.model.user.User
import kotlin.coroutines.resume
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await

class ChatRepositoryFirestore(private val firestore: FirebaseFirestore) : ChatRepository {

  private val messagesCollection = firestore.collection("messages")

  override suspend fun fetchMessages(): List<Message> {
    return try {
      val querySnapshot =
          messagesCollection.orderBy("timestamp", Query.Direction.ASCENDING).get().await()
      querySnapshot.documents.mapNotNull { document -> document.toObject(Message::class.java) }
    } catch (e: Exception) {
      emptyList() // Return an empty list on error
    }
  }

  override suspend fun getOrCreateChat(friendId: String, userId: String): String {
    var chatId = ""

    try {
      val chatQuery =
          firestore.collection("chats").whereArrayContains("participants", friendId).get().await()

      for (document in chatQuery.documents) {
        val participants = document.get("participants") as? List<String>
        if (participants != null && participants.contains(userId)) {
          chatId = document.id
          break
        }
      }

      if (chatId.isEmpty()) {
        val newChatRef = firestore.collection("chats").document()
        newChatRef.set(
            mapOf(
                "participants" to listOf(friendId, userId),
                "lastMessage" to "",
                "lastMessageTimestamp" to null))
        chatId = newChatRef.id
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }

    return chatId
  }

  override fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
    var registration: ListenerRegistration? = null

    try {
      registration =
          firestore
              .collection("chats")
              .document(chatId)
              .collection("messages")
              .orderBy("timestamp", Query.Direction.DESCENDING)
              .addSnapshotListener { snapshot, error ->
                if (error != null) {
                  close(error)
                  return@addSnapshotListener
                }

                val messages =
                    snapshot?.documents?.mapNotNull { document ->
                      document.toObject(Message::class.java)
                    } ?: emptyList()
                trySend(messages)
              }
    } catch (e: Exception) {
      close(e)
    }

    awaitClose { registration?.remove() }
  }

  override suspend fun sendMessage(
      chatId: String,
      messageContent: String,
      userSender: User,
      userReceiver: User
  ) {
    val senderId = userSender.uid

    try {
      val message =
          Message(
              senderId = senderId,
              content = messageContent,
              timestamp = Timestamp.now(),
              isReadBy = listOf(senderId))

      firestore.collection("chats").document(chatId).collection("messages").add(message).await()

      firestore
          .collection("chats")
          .document(chatId)
          .update(
              mapOf(
                  "lastMessage" to messageContent,
                  "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                  "lastMessageSenderId" to userSender.uid,
                  "otherUserId" to userReceiver.uid))
          .await()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun getChatSummaries(userId: String): Flow<List<ChatSummary>> = callbackFlow {
    val registration =
        firestore
            .collection("chats")
            .whereArrayContains("participants", userId)
            .addSnapshotListener { chatsSnapshot, error ->
              if (error != null || chatsSnapshot == null) {
                close(error)
                return@addSnapshotListener
              }
              trySend(
                  chatsSnapshot.documents
                      .mapNotNull { it.toObject(ChatSummary::class.java) }
                      .sortedByDescending { it.lastMessageTimestamp?.toDate()?.time ?: 0 })
            }
    awaitClose { registration.remove() }
  }

  override suspend fun getUserAccount(uid: String): User? {
    return suspendCancellableCoroutine { continuation ->
      firestore.collection("Users").document(uid).get().addOnCompleteListener {
        if (it.isSuccessful) {
          continuation.resume(it.result.toObject(User::class.java))
        } else continuation.resume(null)
      }
    }
  }

  override suspend fun deleteAllMessages(userId: String) {
    try {
      // First, get all chat documents where the current user is a participant
      val chatSnapshot =
          firestore.collection("chats").whereArrayContains("participants", userId)
              .get().await()
      for (chatDocument in chatSnapshot.documents) {
        // For each chat document, get its "messages" subcollection

        val messagesSnapshot = chatDocument.reference.collection("messages")
            .get().await()
        for (messageDocument in messagesSnapshot.documents) {
          messageDocument.reference.delete().await()
        }
      }
    } catch (e: Exception) {
      throw Exception("Deletion failed", e)
    }
  }
}
