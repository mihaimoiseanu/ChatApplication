package com.kroncoders.android.networking.call

import com.kroncoders.android.networking.WebSocketMessagingService
import com.kroncoders.android.networking.models.CallMessageType
import com.kroncoders.android.networking.models.WebSocketCallMessage
import com.kroncoders.android.networking.models.WebSocketFrame
import com.kroncoders.android.networking.models.WebSocketFrameType
import com.kroncoders.android.networking.webrtc.session.WebRtcSessionManager
import com.kroncoders.android.storage.datastore.ChatDataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

class CallServiceImpl(
    private val chatDataStore: ChatDataStore,
    private val json: Json,
    private val webSocketMessagingService: WebSocketMessagingService,
    private val webRtcSessionManager: WebRtcSessionManager
) : CallService, CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO

    private val _callState: MutableStateFlow<CallState> = MutableStateFlow(CallState.Inactive)
    override val callState: SharedFlow<CallState> = _callState

    private val internalCallState: MutableStateFlow<InternalCallState> = MutableStateFlow(InternalCallState.Inactive)
    private var iceCandidateJob: Job? = null

    init {
        internalCallState
            .onEach { state ->
                when (state) {
                    InternalCallState.Inactive -> handleInternalInactiveState()
                    is InternalCallState.Calling -> handleInternalCallingState(state.conversationId)
                    is InternalCallState.SDPAnswer -> handleInternalSDPAnswer(state.conversationId, state.sdpAnswer)
                    is InternalCallState.Busy -> handleInternalBusyState(state.conversationId)
                    is InternalCallState.Called -> handleInternalCalledState(state.conversationId)
                    is InternalCallState.InCall -> handleInternalInCallState(state.conversationId)
                    is InternalCallState.SDPOffer -> handleInternalSDPOffer(state.conversationId, state.sdpOffer)
                    is InternalCallState.End -> handleInternalEndState(state.conversationId)
                }
            }
            .launchIn(this)

        webSocketMessagingService
            .frameStream
            .filter { it.type == WebSocketFrameType.CallMessage }
            .onEach { handleCallFrame(it.content) }
            .launchIn(this)
    }

    override fun makeCall(conversationId: Long) {
        internalCallState.value = InternalCallState.Calling(conversationId)
    }

    override fun acceptCall(conversationId: Long, acceptCall: Boolean) {
        launch {
            if (acceptCall) {
                acceptCall(conversationId)
            } else {
                internalCallState.emit(InternalCallState.Busy(conversationId))
                delay(1_000)
                internalCallState.emit(InternalCallState.Inactive)
            }
        }
    }

    override fun endCall(conversationId: Long) {
        launch {
            sendEndCallFrame(conversationId)
            internalCallState.emit(InternalCallState.End(conversationId))
        }
    }

    private suspend fun acceptCall(conversationId: Long) {
        val callMessageJson = WebSocketCallMessage(
            userId = chatDataStore.userId.first(),
            conversationId = conversationId,
            messageType = CallMessageType.AcceptCall
        ).let { json.encodeToString(it) }
        val webSocketFrame = WebSocketFrame(WebSocketFrameType.CallMessage, callMessageJson)
        webSocketMessagingService.sendTextFrame(webSocketFrame)
        startConnection(conversationId)
    }

    //region Internal call state
    private suspend fun handleInternalInactiveState() = coroutineScope {
        launch { _callState.emit(CallState.Inactive) }
    }

    private suspend fun handleInternalCallingState(conversationId: Long) = coroutineScope {
        val callMessageJson = WebSocketCallMessage(
            userId = chatDataStore.userId.first(),
            conversationId = conversationId,
            messageType = CallMessageType.Calling
        ).let { json.encodeToString(it) }
        val webSocketFrame = WebSocketFrame(WebSocketFrameType.CallMessage, callMessageJson)
        webSocketMessagingService.sendTextFrame(webSocketFrame)
        _callState.emit(CallState.Calling(conversationId))
    }

    private suspend fun handleInternalCalledState(conversationId: Long) = coroutineScope {
        _callState.emit(CallState.Called(conversationId))
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
        webSocketMessagingService.sendTextFrame(webSocketFrame)
    }

    private suspend fun handleInternalSDPAnswer(conversationId: Long, sdpAnswer: String) {
        webRtcSessionManager.handleAnswer(sdpAnswer)
        internalCallState.emit(InternalCallState.InCall(conversationId))
    }

    private suspend fun handleInternalInCallState(conversationId: Long) {
        _callState.emit(CallState.InCall(conversationId))
    }

    private suspend fun handleInternalBusyState(conversationId: Long) = coroutineScope {
        val callMessageJson = WebSocketCallMessage(
            userId = chatDataStore.userId.first(),
            conversationId = conversationId,
            messageType = CallMessageType.Busy
        ).let { json.encodeToString(it) }
        val webSocketFrame = WebSocketFrame(WebSocketFrameType.CallMessage, callMessageJson)
        webSocketMessagingService.sendTextFrame(webSocketFrame)
    }

    private suspend fun handleInternalEndState(conversationId: Long) {
        if (internalCallState.value !is InternalCallState.InCall) return
        val callMessageJson = WebSocketCallMessage(
            userId = chatDataStore.userId.first(),
            conversationId = conversationId,
            messageType = CallMessageType.End,
        ).let { json.encodeToString(it) }
        val webSocketFrame = WebSocketFrame(WebSocketFrameType.CallMessage, callMessageJson)
        webSocketMessagingService.sendTextFrame(webSocketFrame)
        _callState.emit(CallState.End)
        endConnection()
        _callState.emit(CallState.Inactive)
    }
    //endregion

    private suspend fun handleCallFrame(message: String) {
        val webSocketCallMessage: WebSocketCallMessage = json.decodeFromString(message)
        when (webSocketCallMessage.messageType) {
            CallMessageType.OfferSDP -> handleSDPOffer(webSocketCallMessage)
            CallMessageType.AnswerSDP -> handleSDPAnswer(webSocketCallMessage)
            CallMessageType.IceSDP -> handleSDPIce(webSocketCallMessage)
            CallMessageType.Calling -> handleIncomingCall(webSocketCallMessage)
            CallMessageType.AcceptCall -> handleAcceptCall(webSocketCallMessage)
            CallMessageType.Busy -> handleBusy(webSocketCallMessage)
            CallMessageType.End -> handleEndCall(webSocketCallMessage)
        }
    }

    //region WebSocket call frames
    private suspend fun handleSDPOffer(callMessage: WebSocketCallMessage) {
        internalCallState.emit(
            InternalCallState.SDPOffer(
                callMessage.conversationId,
                callMessage.sdp
            )
        )
    }

    private suspend fun handleSDPAnswer(callMessage: WebSocketCallMessage) {
        internalCallState.emit(
            InternalCallState.SDPAnswer(
                callMessage.conversationId,
                callMessage.sdp
            )
        )
    }

    private suspend fun handleAcceptCall(callMessage: WebSocketCallMessage) {
        val conversationId = callMessage.conversationId
        //The other user answered so we start creating the webrtc connection
        startConnection(conversationId)
        val sdpOffer = webRtcSessionManager.getOffer()
        val callMessageJson = WebSocketCallMessage(
            userId = chatDataStore.userId.first(),
            conversationId = conversationId,
            messageType = CallMessageType.OfferSDP,
            sdp = sdpOffer
        ).let { json.encodeToString(it) }
        val webSocketFrame = WebSocketFrame(WebSocketFrameType.CallMessage, callMessageJson)
        webSocketMessagingService.sendTextFrame(webSocketFrame)
    }

    private suspend fun handleSDPIce(callMessage: WebSocketCallMessage) {
        val sdpICE = callMessage.sdp
        webRtcSessionManager.handleIce(sdpICE)
    }

    private suspend fun handleBusy(callMessage: WebSocketCallMessage) {
        internalCallState.emit(InternalCallState.Busy(callMessage.conversationId))
    }

    private suspend fun handleEndCall(callMessage: WebSocketCallMessage) {
        internalCallState.emit(InternalCallState.End(callMessage.conversationId))
    }

    private suspend fun handleIncomingCall(callMessage: WebSocketCallMessage) {
        if (internalCallState.value != InternalCallState.Inactive) {
            handleInternalBusyState(callMessage.conversationId)
            return
        }
        internalCallState.emit(InternalCallState.Called(callMessage.conversationId))
    }

    private suspend fun startConnection(conversationId: Long) {
        iceCandidateJob = webRtcSessionManager
            .iceCandidateStream
            .onEach { iceSDP -> sendIceCandidate(conversationId, iceSDP) }
            .launchIn(this)

        webRtcSessionManager.createSession()
        _callState.emit(CallState.Connecting(conversationId))
    }

    private suspend fun sendEndCallFrame(conversationId: Long) {
        val callMessageJson = WebSocketCallMessage(
            userId = chatDataStore.userId.first(),
            conversationId = conversationId,
            messageType = CallMessageType.End,
        ).let { json.encodeToString(it) }
        val webSocketFrame = WebSocketFrame(WebSocketFrameType.CallMessage, callMessageJson)
        webSocketMessagingService.sendTextFrame(webSocketFrame)
    }

    private fun endConnection() {
        iceCandidateJob?.cancel()
        iceCandidateJob = null
        webRtcSessionManager.disconnect()
    }

    private suspend fun sendIceCandidate(conversationId: Long, iceSDP: String) {
        val callMessageJson = WebSocketCallMessage(
            userId = chatDataStore.userId.first(),
            conversationId = conversationId,
            messageType = CallMessageType.IceSDP,
            sdp = iceSDP
        ).let { json.encodeToString(it) }
        val webSocketFrame = WebSocketFrame(WebSocketFrameType.CallMessage, callMessageJson)
        webSocketMessagingService.sendTextFrame(webSocketFrame)
    }
}