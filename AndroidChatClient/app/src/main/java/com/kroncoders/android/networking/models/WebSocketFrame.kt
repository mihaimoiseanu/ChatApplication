package com.kroncoders.android.networking.models

@kotlinx.serialization.Serializable
data class WebSocketFrame(
    val type: WebSocketFrameType,
    val content: String
)

enum class WebSocketFrameType {
    TextMessage,
    CallMessage,
    PresenceMessage
}