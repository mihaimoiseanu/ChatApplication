package com.kroncoders.service

import com.kroncoders.models.Conversation
import com.kroncoders.persistance.ConversationRepository
import com.kroncoders.persistance.UsersConversationsRepository

class ConversationService(
    private val conversationRepository: ConversationRepository,
    private val usersConversationsRepository: UsersConversationsRepository
) {

    suspend fun createConversation(conversation: Conversation): Conversation {
        val conversationId = conversationRepository.create(conversation.name)
        conversation.users.forEach { user ->
            usersConversationsRepository.create(user, conversationId)
        }
        val userForConversation = usersConversationsRepository.readUsersForConversation(conversationId)
        return conversationRepository.read(conversationId).copy(users = userForConversation)
    }

    suspend fun retrieveConversation(conversationId: Long): Conversation {
        val users = usersConversationsRepository.readUsersForConversation(conversationId)
        return conversationRepository.read(conversationId).copy(users = users)
    }

    suspend fun updateConversation(conversationId: Long, conversation: Conversation): Conversation {
        conversationRepository.update(conversationId, conversation)
        val users = conversation.users
        usersConversationsRepository.deleteUserFromConversation(conversationId, conversation.users)
        val usersInConversation = usersConversationsRepository.readUsersForConversation(conversationId)
        val usersToInsert = users.filter { !usersInConversation.contains(it) }
        usersToInsert.forEach { usersConversationsRepository.create(it, conversationId) }
        val userForConversation = usersConversationsRepository.readUsersForConversation(conversationId)
        return conversationRepository.read(conversationId).copy(users = userForConversation)
    }
}