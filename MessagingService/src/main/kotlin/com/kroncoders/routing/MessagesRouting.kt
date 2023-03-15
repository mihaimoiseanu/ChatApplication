package com.kroncoders.routing

import com.kroncoders.persistance.MessagesRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.messagesRouting() {

    val messagesRepository by inject<MessagesRepository>()

    get("conversations/{conversationId}/messages") {
        val conversationId = call.parameters["conversationId"]?.toLong()
            ?: throw IllegalArgumentException("Conversation Id is missing")
        val messages = messagesRepository.readMessagesForConversation(conversationId = conversationId)
        call.respond(HttpStatusCode.OK, messages)
    }

}