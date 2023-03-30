package com.kroncoders.android.networking.models

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketFrame(
    val type: WebSocketFrameType,
    val content: String
)

@Serializable(with = FrameTypeSerializer::class)
enum class WebSocketFrameType {
    TextMessage,
    CallMessage,
    PresenceMessage
}

private class FrameTypeSerializer : EnumAsIntSerializer<WebSocketFrameType>(
    serialName = "WebSocketFrameType",
    serialize = { it.ordinal },
    deserialize = { WebSocketFrameType.values()[it] }
)