package com.kroncoders.models

import kotlinx.serialization.Serializable

@Serializable
data class WebRTCMessage(
    val userId: Long,
    val conversationId: Long,
    val messageType: WebRTCMessageType,
    val sdp: String
)

enum class WebRTCMessageType {
    Calling, AcceptCall, Busy, OfferSDP, AnswerSDP, IceSDP
}


