package com.kroncoders.android.networking.webrtc.session

import com.kroncoders.android.networking.webrtc.peer.StreamPeerConnectionFactory
import kotlinx.coroutines.flow.SharedFlow
import org.webrtc.IceCandidate
import org.webrtc.VideoTrack

interface WebRtcSessionManager {

    val peerConnectionFactory: StreamPeerConnectionFactory

    val iceCandidateStream: SharedFlow<IceCandidate>

    val localVideoTrackStream: SharedFlow<VideoTrack>

    val remoteVideoTrackStream: SharedFlow<VideoTrack>

    fun createSession()

    suspend fun getAnswerToOffer(sdp: String): String

    suspend fun getOffer(): String

    suspend fun handleAnswer(sdp: String)

    suspend fun handleIce(iceMessage: String)

    fun flipCamera()

    fun enableMicrophone(enabled: Boolean)

    fun enableCamera(enabled: Boolean)

    fun disconnect()
}