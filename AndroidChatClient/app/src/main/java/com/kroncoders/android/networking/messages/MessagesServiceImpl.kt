package com.kroncoders.android.networking.messages

import com.kroncoders.android.networking.WebSocketMessagingService
import com.kroncoders.android.networking.models.NetworkMessage
import com.kroncoders.android.networking.models.WebSocketFrame
import com.kroncoders.android.networking.models.WebSocketFrameType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

class MessagesServiceImpl(
    private val webSocketMessagingService: WebSocketMessagingService,
    private val json: Json,
) : MessagesService, CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO

    private val _messagesStream: MutableSharedFlow<NetworkMessage> = MutableSharedFlow(replay = 5, extraBufferCapacity = 10)
    override val messagesStream: SharedFlow<NetworkMessage> = _messagesStream

    init {
        webSocketMessagingService
            .frameStream
            .filter { it.type == WebSocketFrameType.TextMessage }
            .onEach { handleMessageFrame(it.content) }
            .launchIn(this)
    }

    override fun sendTextMessage(networkMessage: NetworkMessage) {
        val messageJson = json.encodeToString(networkMessage)
        val webSocketFrame = WebSocketFrame(WebSocketFrameType.TextMessage, messageJson)
        webSocketMessagingService.sendTextFrame(webSocketFrame)
    }

    private suspend fun handleMessageFrame(message: String) {
        val networkMessage: NetworkMessage = json.decodeFromString(message)
        _messagesStream.emit(networkMessage)
    }
}