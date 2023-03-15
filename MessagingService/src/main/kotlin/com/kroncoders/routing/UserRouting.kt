package com.kroncoders.routing

import com.kroncoders.models.User
import com.kroncoders.persistance.ConversationRepository
import com.kroncoders.persistance.UserRepository
import com.kroncoders.persistance.UsersConversationsRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.koin.ktor.ext.inject

fun Route.userRouting() {

    val userRepository by inject<UserRepository>()
    val usersConversationsRepository by inject<UsersConversationsRepository>()
    val conversationRepository by inject<ConversationRepository>()

    get("/user/{id?}") {
        val userId = call.parameters["id"]?.toLong()
        if (userId == null) {
            val users = userRepository.readAll()
            call.respond(HttpStatusCode.OK, users)
        } else {
            val user = userRepository.read(userId) ?: throw IllegalStateException("User doesn't exist")
            call.respond(HttpStatusCode.OK, user)
        }
    }

    post("/user") {
        val user = call.receive<User>()
        val existingUser = userRepository.read(user.userName) ?: run {
            val savedUserId = userRepository.create(user)
            userRepository.read(savedUserId) ?: throw IllegalStateException("Couldn't save user")
        }
        call.respond(HttpStatusCode.Created, existingUser)
    }

    get("/user/{id}/conversations") {
        val userId = call.parameters["id"]?.toLong() ?: throw MissingRequestParameterException("invalid User Id")
        val userConversations = conversationRepository.getUserConversations(userId).map { conversation ->
            async(Dispatchers.IO) {
                val conversationUsers = usersConversationsRepository.readUsersForConversation(conversation.id!!)
                conversation.copy(users = conversationUsers)
            }
        }.awaitAll()
        call.respond(HttpStatusCode.OK, userConversations)
    }
}