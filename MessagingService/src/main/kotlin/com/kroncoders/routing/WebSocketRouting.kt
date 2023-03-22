package com.kroncoders.routing

import com.kroncoders.service.SessionService
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.koin.ktor.ext.inject


fun Route.messagesWebSocketRouting() {

    val sessionService by inject<SessionService>()


    webSocket("/ws/{userId}") { // websocketSession
        val userId = call.parameters["userId"]?.toLong() ?: throw IllegalStateException("userId missing")
        try {
            sessionService.onSessionStarted(userId, this)

            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> sessionService.onMessages(frame.readText())
                    else -> Unit
                }
            }

        } catch (e: ClosedReceiveChannelException) {
            println("onClose $userId ")
            sessionService.onSessionClose(userId)
        } catch (e: Throwable) {
            println("onError $userId $e")
            sessionService.onSessionClose(userId)
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