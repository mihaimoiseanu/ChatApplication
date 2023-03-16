package com.kroncoders.routing

import com.kroncoders.service.MessagesService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.messagesRouting() {

    val messagesService by inject<MessagesService>()

    get("conversations/{conversationId}/messages") {
        val conversationId = call.parameters["conversationId"]?.toLong()
            ?: throw IllegalArgumentException("Conversation Id is missing")
        val messages = messagesService.getMessagesForConversation(conversationId = conversationId)
        call.respond(HttpStatusCode.OK, messages)
    }

}