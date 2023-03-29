package com.kroncoders.android.networking

import com.kroncoders.android.networking.models.WebSocketFrame
import com.kroncoders.android.storage.datastore.ChatDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketMessagingService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val chatDataStore: ChatDataStore,
    private val json: Json
) {

    private var webSocket: WebSocket? = null
    private val messagingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _frameStream = MutableSharedFlow<WebSocketFrame>()
    val frameStream: SharedFlow<WebSocketFrame> = _frameStream


    fun connectToWebSocket() {
        messagingScope.launch {
            val userId = chatDataStore.userId.first()
            val request = Request.Builder().url("$BaseUrl/ws/$userId").build()
            webSocket = okHttpClient.newWebSocket(request, MessagingSocketListener())
        }
    }

    fun sendTextFrame(webSocketFrame: WebSocketFrame) {
        val textFrame = json.encodeToString(webSocketFrame)
        webSocket?.send(textFrame)
    }

    private inner class MessagingSocketListener : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            messagingScope.launch {
                val webSocketFrame: WebSocketFrame = json.decodeFromString(text)
                _frameStream.emit(webSocketFrame)
            }
        }
    }
}