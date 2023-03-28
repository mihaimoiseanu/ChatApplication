package com.kroncoders.android.networking

sealed interface InternalCallState {
    object Inactive : InternalCallState
    data class Calling(val conversationId: Long) : InternalCallState
    data class Called(val conversationId: Long) : InternalCallState
    data class Offering(val conversationId: Long, val offeringSDP: String) : InternalCallState
    data class Answer(val conversationId: Long, val answerSDP: String) : InternalCallState
    data class InCall(val conversationId: Long) : InternalCallState
    data class Busy(val conversationId: Long) : InternalCallState
}