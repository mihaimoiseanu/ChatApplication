package com.kroncoders.service

import com.kroncoders.models.Conversation
import com.kroncoders.persistance.ConversationRepository
import com.kroncoders.persistance.UsersConversationsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class ConversationService(
    private val conversationRepository: ConversationRepository,
    private val usersConversationsRepository: UsersConversationsRepository
) {

    suspend fun createConversation(conversation: Conversation): Conversation {
        if (conversation.users.isEmpty()) throw IllegalStateException("There's need to be at least one user")

        val existingConversation = checkIfConversationAlreadyExists(conversation)
        if (existingConversation != null) return existingConversation

        val conversationId = conversationRepository.create(conversation.name)
        conversation.users.forEach { user ->
            usersConversationsRepository.create(user, conversationId)
        }
        val userForConversation = usersConversationsRepository.readUsersForConversation(conversationId)
        return conversationRepository.read(conversationId).copy(users = userForConversation)
    }

    private suspend fun checkIfConversationAlreadyExists(conversation: Conversation): Conversation? {
        if (conversation.users.size > 2) return null // if we have more than 2 users create a new conversation
        if (conversation.users.size == 1) {
            val user = conversation.users.first()
            val userConversations = getUserConversations(user)
            return userConversations.first { it.users equalsIgnoreOrder listOf(user) }
        }
        val userInConversation = conversation.users
        val (firstUserConversations, secondUserConversations) = userInConversation.map { getUserConversations(it) }
        val conversationForFirstUser =
            firstUserConversations.firstOrNull { it.users equalsIgnoreOrder userInConversation }
        val conversationForSecondUser =
            secondUserConversations.firstOrNull { it.users equalsIgnoreOrder userInConversation }
        if (conversationForFirstUser == null && conversationForSecondUser == null) return null
        if (conversationForFirstUser == conversationForSecondUser) return conversationForFirstUser
        //TODO handle if one user exited the conversation
        return null
    }

    infix fun <T> List<T>.equalsIgnoreOrder(other: List<T>) = this.size == other.size && this.toSet() == other.toSet()

    suspend fun retrieveConversation(conversationId: Long): Conversation {
        val users = usersConversationsRepository.readUsersForConversation(conversationId)
        return conversationRepository.read(conversationId).copy(users = users)
    }

    suspend fun getUserConversations(userId: Long): List<Conversation> = coroutineScope {
        conversationRepository.getUserConversations(userId).map { conversation ->
            async(Dispatchers.IO) {
                val conversationUsers = usersConversationsRepository.readUsersForConversation(conversation.id!!)
                conversation.copy(users = conversationUsers)
            }
        }.awaitAll()
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