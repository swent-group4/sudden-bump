package com.swent.suddenbump.model.chat

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChatRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val messagesCollection = firestore.collection("messages")

    // Fetch messages from Firestore, ordered by timestamp
    suspend fun fetchMessages(): List<Message> {
        return try {
            val querySnapshot = messagesCollection
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject(Message::class.java)
            }
        } catch (e: Exception) {
            emptyList() // In case of an error, return an empty list
        }
    }

    suspend fun getOrCreateChat(userId: String): String {
        val firestore = FirebaseFirestore.getInstance()
        val userId2 = FirebaseAuth.getInstance().currentUser?.uid ?: return ""
        var chatId = ""

        try {
            // Query for existing chat between the two users
            val chatQuery = firestore.collection("chats")
                .whereArrayContains("participants", userId)
                .get()
                .await()

            // Check if any chat contains both userId1 and userId2
            for (document in chatQuery.documents) {
                val participants = document.get("participants") as? List<String>
                if (participants != null && participants.contains(userId2)) {
                    chatId = document.id
                    break
                }
            }

            // If no chat exists, create a new one
            if (chatId.isEmpty()) {
                val newChatRef = firestore.collection("chats").document()
                newChatRef.set(mapOf(
                    "participants" to listOf(userId, userId2),
                    "lastMessage" to "",
                    "lastMessageTimestamp" to null
                ))
                chatId = newChatRef.id
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return chatId
    }

    // Use Firestore to get a real-time stream of messages for a given chat
    fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val firestore = FirebaseFirestore.getInstance()
        var registration: ListenerRegistration? = null

        try {
            // Listen for real-time updates on the messages collection under the specific chatId
            registration = firestore.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Order messages by timestamp
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error) // Terminate the flow on error
                        return@addSnapshotListener
                    }

                    val messages = snapshot?.documents?.mapNotNull { document ->
                        document.toObject(Message::class.java)
                    } ?: emptyList()

                    trySend(messages) // Send the updated message list to the flow
                }
        } catch (e: Exception) {
            close(e) // Close flow in case of exception
        }

        awaitClose { registration?.remove() } // Clean up the listener when the flow is closed
    }

    // Add a new message to Firestore
    suspend fun sendMessage(chatId: String, messageContent: String, username: String) {
        val firestore = FirebaseFirestore.getInstance()
        val senderId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val senderName = FirebaseAuth.getInstance().currentUser?.displayName ?: return

        try {
            // Add the message to the Messages subcollection
            val message = Message(
                senderId = senderId,
                content = messageContent,
                timestamp = Timestamp.now(),
                isReadBy = listOf(senderId)
            )

            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message)
                .await()

            // Update the last message and timestamp in the chat document
            firestore.collection("chats")
                .document(chatId)
                .update(mapOf(
                    "lastMessage" to messageContent,
                    "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                    "lastMessageSender" to senderName,
                    "otherUserName"  to username
                ))
                .await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun markMessagesAsRead(chatId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        try {
            // Fetch all unread messages (those that don't contain userId in isReadBy)
            val messagesSnapshot = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .whereNotIn("readBy", listOf(userId))
                .get()
                .await()

            // Mark each message as read by adding userId to isReadBy
            for (document in messagesSnapshot.documents) {
                val isReadBy = document.get("readBy") as? MutableList<String> ?: mutableListOf()
                if (!isReadBy.contains(userId)) {
                    isReadBy.add(userId)
                    firestore.collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .document(document.id)
                        .update("readBy", isReadBy)
                        .await()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getUnreadMessagesCount(chatId: String, userId: String): Int {
        val firestore = FirebaseFirestore.getInstance()
        var unreadCount = 0

        try {
            // Retrieve all messages in the chat
            val messagesSnapshot = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .get()
                .await()

            // Filter messages that do not contain userId in the isReadBy array
            unreadCount = messagesSnapshot.documents.count { document ->
                val isReadBy = document["readBy"] as? List<*> ?: emptyList<Any>()
                userId !in isReadBy
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return unreadCount
    }

    fun getChatSummaries(): Flow<List<ChatSummary>> = callbackFlow {
        val firestore = FirebaseFirestore.getInstance()
        var registration: ListenerRegistration? = null
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@callbackFlow

        try {
            registration = firestore.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener { chatsSnapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    if (chatsSnapshot != null && !chatsSnapshot.isEmpty) {
                        val chatSummaries = mutableListOf<ChatSummary>()

                        // Use a coroutine scope to retrieve unread counts
                        runBlocking {
                            for (chatDoc in chatsSnapshot.documents) {
                                val lastMessage = chatDoc.getString("lastMessage") ?: ""
                                val lastMessageSender = chatDoc.getString("lastMessageSender") ?: ""
                                val lastMessageTimestamp = chatDoc.getTimestamp("lastMessageTimestamp")
                                val unreadCount = withContext(Dispatchers.IO) {
                                    getUnreadMessagesCount(chatDoc.id, currentUserId)
                                }
                                val participants = chatDoc.get("participants") as? List<String> ?: emptyList()
                                val usernmame = chatDoc.getString("otherUserName")?:""

                                chatSummaries.add(ChatSummary(lastMessage, lastMessageSender, lastMessageTimestamp, unreadCount, participants,usernmame))
                            }
                        }

                        trySend(chatSummaries).isSuccess
                    } else {
                        trySend(emptyList()).isSuccess // Emit an empty list if no chats found
                    }
                }
        } catch (e: Exception) {
            close(e)
        }

        awaitClose { registration?.remove() }
    }
}