package com.kroncoders.models

import kotlinx.serialization.Serializable

@Serializable
data class WebRTCMessage(
    val userId: Long,
    val conversationId: Long,
    val messageType: WebRTCMessageType,
    val sdp: String
)

@Serializable(with = MessageTypeSerializer::class)
enum class WebRTCMessageType {
    Calling, AcceptCall, Busy, OfferSDP, AnswerSDP, IceSDP, End
}

private class MessageTypeSerializer : EnumAsIntSerializer<WebRTCMessageType>(
    serialName = "MessageTypeSerializer",
    serialize = { it.ordinal },
    deserialize = { WebRTCMessageType.values()[it] }
)

