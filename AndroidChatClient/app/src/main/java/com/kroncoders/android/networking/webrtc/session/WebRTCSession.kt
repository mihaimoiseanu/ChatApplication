package com.kroncoders.android.networking.webrtc.session

import org.webrtc.*

class WebRTCSession(
    val videoCapturer: VideoCapturer,
    val videoSource: VideoSource,
    val audioSource: AudioSource,

    val localAudioTrack: AudioTrack,
    val localVideoTrack: VideoTrack,

    val peerConnection: PeerConnection
)