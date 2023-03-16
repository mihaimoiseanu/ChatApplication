package com.kroncoders.messaging

import com.kroncoders.models.Message
import com.kroncoders.service.MessagesService
import com.kroncoders.service.UserService
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import java.util.concurrent.ConcurrentHashMap

val sessionModule = module {
    single { SessionManager(get(), get(), get()) }
}

class SessionManager(
    private val json: Json,
    private val messagesService: MessagesService,
    private val userService: UserService
) {

    private val sessionManagerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val clients = ConcurrentHashMap<Long, DefaultWebSocketServerSession>()

    fun onSessionStarted(userId: Long, sessions: DefaultWebSocketServerSession) {
        clients[userId] = sessions
    }

    fun onMessages(message: String) {
        sessionManagerScope.launch {
            val incomingMessage: Message = json.decodeFromString(message)
            val savedMessage = messagesService.insertMessage(message = incomingMessage)
            val usersInConversation = userService.getUsersIdsForConversation(savedMessage.conversationId)
            val savedMessageJson = json.encodeToString(savedMessage)
            usersInConversation.forEach { userId ->
                clients[userId]?.send(savedMessageJson)
            }
        }
    }

    fun onSessionClose(userId: Long) {
        clients.remove(userId)
    }

    private fun DefaultWebSocketServerSession.send(message: String) {
        sessionManagerScope.launch { this@send.send(Frame.Text(message)) }
    }
}