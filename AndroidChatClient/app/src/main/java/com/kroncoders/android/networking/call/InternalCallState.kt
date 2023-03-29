package com.kroncoders.android.networking.call

sealed interface InternalCallState {
    object Inactive : InternalCallState
    data class Calling(val conversationId: Long) : InternalCallState
    data class Called(val conversationId: Long) : InternalCallState
    data class SDPOffer(val conversationId: Long, val sdpOffer: String) : InternalCallState
    data class SDPAnswer(val conversationId: Long, val sdpAnswer: String) : InternalCallState
    data class InCall(val conversationId: Long) : InternalCallState
    data class Busy(val conversationId: Long) : InternalCallState
    data class End(val conversationId: Long) : InternalCallState
}