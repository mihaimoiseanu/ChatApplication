package com.kroncoders.android.networking

sealed interface CallState {
    object Inactive : CallState
    data class Calling(val conversationId: Long) : CallState
    data class Called(val conversationId: Long) : CallState
    data class Connecting(val conversationId: Long) : CallState
    data class InCall(val conversationId: Long) : CallState
    object Busy : CallState
}