package com.kroncoders.android.networking

import com.kroncoders.android.networking.models.*
import com.kroncoders.android.networking.webrtc.session.WebRtcSessionManager
import com.kroncoders.android.storage.datastore.ChatDataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
    private val webRtcSessionManager: WebRtcSessionManager,
    private val json: Json
) {

    private var webSocket: WebSocket? = null
    private val messagingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val messagesStream: MutableSharedFlow<NetworkMessage> = MutableSharedFlow(replay = 5, extraBufferCapacity = 10)
    val callState: MutableStateFlow<CallState> = MutableStateFlow(CallState.Inactive)

    private val internalCallState: MutableStateFlow<InternalCallState> = MutableStateFlow(InternalCallState.Inactive)

    init {
        internalCallState
            .onEach { state ->
                when (state) {
                    InternalCallState.Inactive -> handleInternalInactiveState()
                    is InternalCallState.Calling -> handleInternalCallingState(state.conversationId)
                    is InternalCallState.Answer -> handleInternalSDPAnswer(state.conversationId, state.answerSDP)
                    is InternalCallState.Busy -> handleInternalBusyState(state.conversationId)
                    is InternalCallState.Called -> handleInternalCalledState(state.conversationId)
                    is InternalCallState.InCall -> handleInternalInCallState(state.conversationId)
                    is InternalCallState.Offering -> handleInternalSDPOffer(state.conversationId, state.offeringSDP)
                }
            }
            .launchIn(messagingScope)
    }

    fun connectToWebSocket() {
        messagingScope.launch {
            val userId = chatDataStore.userId.first()
            val request = Request.Builder().url("$BaseUrl/ws/$userId").build()
            webSocket = okHttpClient.newWebSocket(request, MessagingSocketListener())
        }
    }

    fun sendTextMessage(networkMessage: NetworkMessage) {
        val messageJson = json.encodeToString(networkMessage)
        val webSocketFrame = WebSocketFrame(WebSocketFrameType.TextMessage, messageJson)
        sendTextFrame(webSocketFrame)
    }

    private fun sendTextFrame(webSocketFrame: WebSocketFrame) {
        val textFrame = json.encodeToString(webSocketFrame)
        webSocket?.send(textFrame)
    }

    private inner class MessagingSocketListener : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            messagingScope.launch {
                val webSocketFrame: WebSocketFrame = json.decodeFromString(text)
                when (webSocketFrame.type) {
                    WebSocketFrameType.TextMessage -> handleMessageFrame(webSocketFrame.content)
                    WebSocketFrameType.CallMessage -> handleCallFrame(webSocketFrame.content)
                    WebSocketFrameType.PresenceMessage -> TODO()
                }

            }
        }
    }

    private suspend fun handleMessageFrame(message: String) {
        val networkMessage: NetworkMessage = json.decodeFromString(message)
        messagesStream.emit(networkMessage)
    }

    private suspend fun handleCallFrame(message: String) {
        val webSocketCallMessage: WebSocketCallMessage = json.decodeFromString(message)
        when (webSocketCallMessage.messageType) {
            CallMessageType.OfferSDP -> handleSDPOffer(webSocketCallMessage)
            CallMessageType.AnswerSDP -> handleSDPAnswer(webSocketCallMessage)
            CallMessageType.IceSDP -> handleSDPIce(webSocketCallMessage)
            CallMessageType.Calling -> handleIncomingCall(webSocketCallMessage)
            CallMessageType.Answering -> handleAnswer(webSocketCallMessage)
            CallMessageType.Busy -> handleBusy(webSocketCallMessage)
        }
    }

    //region Call
    fun callConversation(conversationId: Long) {
        internalCallState.value = InternalCallState.Calling(conversationId)
    }

    fun answerCall(conversationId: Long, answer: Boolean) {
        messagingScope.launch {
            if (answer) {
                answerCall(conversationId)
            } else {
                internalCallState.emit(InternalCallState.Busy(conversationId))
                delay(1_000)
                internalCallState.emit(InternalCallState.Inactive)
            }
        }
    }

    private suspend fun answerCall(conversationId: Long) {
        val callMessageJson = WebSocketCallMessage(
            userId = chatDataStore.userId.first(),
            conversationId = conversationId,
            messageType = CallMessageType.Answering
        ).let { json.encodeToString(it) }
        val webSocketFrame = WebSocketFrame(WebSocketFrameType.CallMessage, callMessageJson)
        sendTextFrame(webSocketFrame)
        callState.emit(CallState.Connecting(conversationId))
    }

    private suspend fun handleInternalInactiveState() = coroutineScope {
        launch { callState.emit(CallState.Inactive) }
    }

    private suspend fun handleInternalCallingState(conversationId: Long) = coroutineScope {
        val callMessageJson = WebSocketCallMessage(
            userId = chatDataStore.userId.first(),
            conversationId = conversationId,
            messageType = CallMessageType.Calling
        ).let { json.encodeToString(it) }
        val webSocketFrame = WebSocketFrame(WebSocketFrameType.CallMessage, callMessageJson)
        sendTextFrame(webSocketFrame)
        callState.emit(CallState.Calling(conversationId))
    }

    private suspend fun handleInternalCalledState(conversationId: Long) = coroutineScope {
        callState.emit(CallState.Called(conversationId))
    }

    private suspend fun handleIncomingCall(callMessage: WebSocketCallMessage) {
        if (internalCallState.value != InternalCallState.Inactive) {
            handleInternalBusyState(callMessage.conversationId)
            return
        }
        internalCallState.emit(InternalCallState.Called(callMessage.conversationId))
    }

    private suspend fun handleSDPOffer(callMessage: WebSocketCallMessage) {
        internalCallState.emit(InternalCallState.Offering(callMessage.conversationId, callMessage.sdp))
    }

    private suspend fun handleSDPAnswer(callMessage: WebSocketCallMessage) {
        internalCallState.emit(InternalCallState.Answer(callMessage.conversationId, callMessage.sdp))
    }

    private suspend fun handleAnswer(callMessage: WebSocketCallMessage) {
        val conversationId = callMessage.conversationId
        //The other user answered so we start creating the webrtc connection
        callState.emit(CallState.Connecting(conversationId))
        webRtcSessionManager.createSession()
        val sdpOffer = webRtcSessionManager.getOffer()
        val callMessageJson = WebSocketCallMessage(
            userId = chatDataStore.userId.first(),
            conversationId = conversationId,
            messageType = CallMessageType.OfferSDP,
            sdp = sdpOffer
        ).let { json.encodeToString(it) }
        val webSocketFrame = WebSocketFrame(WebSocketFrameType.CallMessage, callMessageJson)
        sendTextFrame(webSocketFrame)
    }

    private suspend fun handleInternalSDPOffer(conversationId: Long, sdpOffer: String) {
        val sdpAnswer = webRtcSessionManager.getAnswerToOffer(sdpOffer)
        val callMessageJson = WebSocketCallMessage(
            userId = chatDataStore.userId.first(),
            conversationId = conversationId,
            messageType = CallMessageType.AnswerSDP,
            sdp = sdpAnswer
        ).let { json.encodeToString(it) }
        val webSocketFrame = WebSocketFrame(WebSocketFrameType.CallMessage, callMessageJson)
        sendTextFrame(webSocketFrame)
    }

    private suspend fun handleInternalSDPAnswer(conversationId: Long, sdpAnswer: String) {
        webRtcSessionManager.handleAnswer(sdpAnswer)

    }

    private suspend fun handleSDPIce(callMessage: WebSocketCallMessage) {
        val conversationId = callMessage.conversationId
        val sdpICE = callMessage.sdp
        webRtcSessionManager.handleIce(sdpICE)
    }

    private suspend fun handleBusy(callMessage: WebSocketCallMessage) {

    }

    private suspend fun handleInternalInCallState(conversationId: Long) {

    }

    private suspend fun handleInternalBusyState(conversationId: Long) = coroutineScope {
        val callMessageJson = WebSocketCallMessage(
            userId = chatDataStore.userId.first(),
            conversationId = conversationId,
            messageType = CallMessageType.Busy
        ).let { json.encodeToString(it) }
        val webSocketFrame = WebSocketFrame(WebSocketFrameType.CallMessage, callMessageJson)
        sendTextFrame(webSocketFrame)
    }
    //endregion
}