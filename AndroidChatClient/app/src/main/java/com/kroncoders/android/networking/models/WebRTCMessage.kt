package com.kroncoders.android.networking.models

import kotlinx.serialization.Serializable

@Serializable
data class WebRTCMessage(
    val userId: Long,
    val conversationId: Long,
    val messageType: WebRTCMessageType,
    val message: String
)

enum class WebRTCMessageType {
    Offer, Answer, ICE
}
