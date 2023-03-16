package com.kroncoders.routing

import com.kroncoders.models.User
import com.kroncoders.service.ConversationService
import com.kroncoders.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.userRouting() {

    val userService by inject<UserService>()
    val conversationService by inject<ConversationService>()

    get("/user/{id?}") {
        val userId = call.parameters["id"]?.toLong()
        if (userId == null) {
            val users = userService.getAllUsers()
            call.respond(HttpStatusCode.OK, users)
        } else {
            val user = userService.getUser(userId)
            call.respond(HttpStatusCode.OK, user)
        }
    }

    post("/user") {
        val user = call.receive<User>()
        val existingUser = userService.createUser(user)
        call.respond(HttpStatusCode.Created, existingUser)
    }

    get("/user/{id}/conversations") {
        val userId = call.parameters["id"]?.toLong() ?: throw MissingRequestParameterException("invalid User Id")
        val userConversations = conversationService.getUserConversations(userId)
        call.respond(HttpStatusCode.OK, userConversations)
    }
}