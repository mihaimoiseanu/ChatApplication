package com.kroncoders.service

import com.kroncoders.models.Message
import com.kroncoders.models.WebRTCMessage
import com.kroncoders.models.WebSocketFrame
import com.kroncoders.models.WebSocketFrameType
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import java.util.concurrent.ConcurrentHashMap

val sessionModule = module {
    single { SessionService(get(), get(), get()) }
}

class SessionService(
    private val json: Json,
    private val messagesService: MessagesService,
    private val userService: UserService
) {

    private val sessionManagerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val clients = ConcurrentHashMap<Long, DefaultWebSocketServerSession>()

    fun onSessionStarted(userId: Long, sessions: DefaultWebSocketServerSession) {
        clients[userId] = sessions
    }

    fun onMessages(textFrame: String) {
        sessionManagerScope.launch {
            val frame: WebSocketFrame = json.decodeFromString(textFrame)
            when (frame.type) {
                WebSocketFrameType.TextMessage -> handleTextMessageFrame(frame.content)
                WebSocketFrameType.CallMessage -> handleWebRTCMessageFrame(frame.content)
                WebSocketFrameType.PresenceMessage -> TODO()
            }
        }
    }

    private suspend fun handleTextMessageFrame(textMessageFrame: String) {
        val incomingMessage: Message = json.decodeFromString(textMessageFrame)
        val savedMessage = messagesService.insertMessage(message = incomingMessage)
        val usersInConversation = userService.getUsersIdsForConversation(savedMessage.conversationId)
        val savedMessageJson = json.encodeToString(savedMessage)
        usersInConversation.forEach { userId ->
            clients[userId]?.send(savedMessageJson)
        }
    }

    private suspend fun handleWebRTCMessageFrame(webRTCMessageFrame: String) = coroutineScope {
        val webRTCMessage: WebRTCMessage = json.decodeFromString(webRTCMessageFrame)
        val conversationUsers =
            userService.getUsersIdsForConversation(webRTCMessage.conversationId) - webRTCMessage.userId
        //TODO check if the users are online
        conversationUsers.forEach { userId ->
            launch {
                clients[userId]?.send(webRTCMessageFrame)
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