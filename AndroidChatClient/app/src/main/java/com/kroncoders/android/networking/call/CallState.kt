package com.kroncoders.android.networking.call

import kotlinx.coroutines.flow.Flow
import org.webrtc.VideoTrack

sealed interface CallState {
    object Inactive : CallState
    data class Calling(val conversationId: Long) : CallState
    data class Called(val conversationId: Long) : CallState
    data class Connecting(val conversationId: Long, val localVideoTrack: Flow<VideoTrack?>) : CallState
    data class InCall(val conversationId: Long, val localVideoTrack: Flow<VideoTrack?>, val remoteVideoTrack: Flow<VideoTrack?>) : CallState
    object Busy : CallState

    object End : CallState
}