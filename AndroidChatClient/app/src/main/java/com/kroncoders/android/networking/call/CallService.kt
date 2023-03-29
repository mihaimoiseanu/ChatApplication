package com.kroncoders.android.networking.call

import kotlinx.coroutines.flow.SharedFlow

interface CallService {

    val callState: SharedFlow<CallState>

    fun makeCall(conversationId: Long)

    fun acceptCall(conversationId: Long, acceptCall: Boolean)

    fun endCall(conversationId: Long)
}