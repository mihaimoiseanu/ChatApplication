package com.kroncoders.android.networking.models

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketCallMessage(
    val userId: Long,
    val conversationId: Long,
    val messageType: CallMessageType,
    val sdp: String = ""
)

enum class CallMessageType {
    Calling, AcceptCall, Busy, OfferSDP, AnswerSDP, IceSDP, End
}
