package com.kroncoders.routing

import com.kroncoders.messaging.SessionManager
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.koin.ktor.ext.inject


fun Route.messagesWebSocketRouting() {

    val sessionManager by inject<SessionManager>()


    webSocket("/ws/{userId}") { // websocketSession
        val userId = call.parameters["userId"]?.toLong() ?: throw IllegalStateException("userId missing")
        try {
            sessionManager.onSessionStarted(userId, this)

            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> sessionManager.onMessages(frame.readText())
                    else -> Unit
                }
            }

        } catch (e: ClosedReceiveChannelException) {
            println("onClose $userId ")
            sessionManager.onSessionClose(userId)
        } catch (e: Throwable) {
            println("onError $userId $e")
            sessionManager.onSessionClose(userId)
        }
        for (frame in incoming) {
            if (frame is Frame.Text) {
                val text = frame.readText()
                outgoing.send(Frame.Text("YOU SAID: $text"))
                if (text.equals("bye", ignoreCase = true)) {
                    close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                }
            }
        }
    }
}