package com.kroncoders.service

import com.kroncoders.models.User
import com.kroncoders.persistance.UserRepository
import com.kroncoders.persistance.UsersConversationsRepository

class UserService(
    private val userRepository: UserRepository,
    private val usersConversationsRepository: UsersConversationsRepository
) {

    suspend fun getUsersIdsForConversation(conversationId: Long): List<Long> {
        return usersConversationsRepository.readUsersForConversation(conversationId)
    }

    suspend fun getAllUsers(): List<User> {
        return userRepository.readAll()
    }

    suspend fun getUser(userId: Long): User {
        return userRepository.read(userId) ?: throw IllegalStateException("User not found")
    }

    suspend fun createUser(user: User): User {
        return userRepository.read(user.userName) ?: run {
            val savedUserId = userRepository.create(user)
            userRepository.read(savedUserId) ?: throw IllegalStateException("User couldn't be created")
        }
    }
}