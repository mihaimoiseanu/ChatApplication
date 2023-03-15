package com.kroncoders.routing

import com.kroncoders.models.Conversation
import com.kroncoders.service.ConversationService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.conversationRouting() {

    val conversationService: ConversationService by inject()

    post("/conversations") {
        val conversationRequest = call.receive<Conversation>()
        val conversationResponse = conversationService.createConversation(conversationRequest)
        call.respond(HttpStatusCode.Created, conversationResponse)
    }

    get("/conversations/{conversationId}") {
        val conversationId = call
            .parameters["conversationId"]
            ?.toLong()
            ?: throw IllegalArgumentException("Missing conversationId")
        val conversation = conversationService.retrieveConversation(conversationId = conversationId)
        call.respond(HttpStatusCode.OK, conversation)
    }

    put("/conversations/{conversationId}") {
        val conversationId = call.parameters["conversationId"]?.toLong() ?: throw IllegalArgumentException("Missing id")
        val conversation = call.receive<Conversation>()
        val conversationResponse = conversationService.updateConversation(conversationId, conversation)
        call.respond(HttpStatusCode.Accepted, conversationResponse)
    }
}