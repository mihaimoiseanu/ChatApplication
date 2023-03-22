package com.kroncoders.models

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketFrame(
    val type: WebSocketFrameType,
    val content: String
)

enum class WebSocketFrameType {
    Message,
    WebRTC,
    PresenceIndicator
}