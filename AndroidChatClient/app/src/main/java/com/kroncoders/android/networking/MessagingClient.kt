package com.kroncoders.android.networking

import com.kroncoders.android.networking.models.NetworkMessage
import com.kroncoders.android.storage.datastore.ChatDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.stateIn
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
class MessagingClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val chatDataStore: ChatDataStore,
    private val json: Json
) {

    private var webSocket: WebSocket? = null
    private val messagingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val messagesFlow = MutableSharedFlow<NetworkMessage>(replay = 5, extraBufferCapacity = 10)

    fun connectToWebSocket() {
        messagingScope.launch {
            val userId = chatDataStore.userId.stateIn(messagingScope).value
            val request = Request.Builder().url("$BaseUrl/ws/$userId").build()
            webSocket = okHttpClient.newWebSocket(request, MessagingSocketListener())
        }
    }

    fun sendMessage(networkMessage: NetworkMessage) {
        val messageJson = json.encodeToString(networkMessage)
        webSocket?.send(messageJson)
    }

    private inner class MessagingSocketListener : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            messagingScope.launch {
                val networkMessage: NetworkMessage = json.decodeFromString(text)
                messagesFlow.emit(networkMessage)
            }
        }
    }
}