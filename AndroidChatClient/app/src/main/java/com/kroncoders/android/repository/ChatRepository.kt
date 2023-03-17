package com.kroncoders.android.repository

import com.kroncoders.android.repository.models.Conversation
import com.kroncoders.android.repository.models.Message
import com.kroncoders.android.repository.models.User
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    suspend fun loginUser(user: User)
    suspend fun logout()
    suspend fun currentUserId(): Long
    fun connectToServer()

    fun getConversations(): Flow<List<Conversation>>
    fun getConversation(conversationId: Long): Flow<Conversation>
    fun getMessagesForConversation(conversationId: Long): Flow<List<Message>>
    fun getAllUsers(): Flow<List<User>>

    suspend fun createConversation(conversation: Conversation)

    suspend fun sendMessage(message: Message)

    suspend fun syncConversation(conversationId: Long)
    suspend fun syncConversations()
    suspend fun syncMessagesForConversation(conversationId: Long)
    suspend fun syncUsers()
    suspend fun updateConversation(conversationId: Long, conversation: Conversation)
}