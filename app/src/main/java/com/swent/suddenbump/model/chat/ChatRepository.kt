package com.swent.suddenbump.model.chat

import kotlinx.coroutines.flow.Flow
import com.swent.suddenbump.model.user.User

interface ChatRepository {
    suspend fun fetchMessages(): List<Message>

    suspend fun getOrCreateChat(userId: String): String

    fun getMessages(chatId: String): Flow<List<Message>>

    suspend fun sendMessage(chatId: String, messageContent: String, username: String)

    fun getChatSummaries(): Flow<List<ChatSummary>>

    suspend fun getUserAccount(uid: String): User?
}
