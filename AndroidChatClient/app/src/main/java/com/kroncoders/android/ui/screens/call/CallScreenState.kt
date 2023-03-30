package com.kroncoders.android.ui.screens.call

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.webrtc.VideoTrack

data class CallScreenModel(
    val userName: String = "",
    val state: CallScreenState = CallScreenState.Inactive,
    val localStream: Flow<VideoTrack?> = emptyFlow(),
    val remoteStream: Flow<VideoTrack?> = emptyFlow(),
    val isCameraEnabled: Boolean = true,
    val isMicrophoneEnabled: Boolean = true,
)

sealed interface CallScreenState {
    object Inactive : CallScreenState
    object Calling : CallScreenState
    object Called : CallScreenState
    object Busy : CallScreenState
    object Finished : CallScreenState
    object Connecting : CallScreenState
    object InCall : CallScreenState
}