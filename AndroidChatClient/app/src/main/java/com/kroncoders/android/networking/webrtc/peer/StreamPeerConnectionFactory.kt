package com.kroncoders.android.networking.webrtc.peer

import android.content.Context
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import org.webrtc.*
import org.webrtc.PeerConnection.Observer
import org.webrtc.PeerConnection.RTCConfiguration
import org.webrtc.audio.JavaAudioDeviceModule
import org.webrtc.audio.JavaAudioDeviceModule.AudioTrackStateCallback
import timber.log.Timber

class StreamPeerConnectionFactory(private val context: Context) {

    val eglBaseContext: EglBase.Context by lazy { EglBase.create().eglBaseContext }

    private val videoDecoderFactory: VideoDecoderFactory
        get() {
            return DefaultVideoDecoderFactory(eglBaseContext)
        }

    // rtcConfig contains STUN and TURN servers list
    val rtcConfig = RTCConfiguration(
        arrayListOf(
            // adding google's standard server
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
    ).apply {
        // it's very important to use new unified sdp semantics PLAN_B is deprecated
        sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
    }

    /**
     * Default encoder factory that supports Simulcast, used to send video tracks to the server.
     */
    private val videoEncoderFactory: VideoEncoderFactory
        get() {
            val hardwareEncoder = HardwareVideoEncoderFactory(eglBaseContext, true, true)
            return SimulcastVideoEncoderFactory(hardwareEncoder, SoftwareVideoEncoderFactory())
        }

    /**
     * Factory that builds all the connections based on the extensive configuration provided under
     * the hood.
     */
    private val factory: PeerConnectionFactory
        get() {
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions
                    .builder(context)
                    .setInjectableLogger({ message, severity, label ->
                        when (severity) {
                            Logging.Severity.LS_VERBOSE -> Timber.v("[onLogMessage] label: $label, message: $message")
                            Logging.Severity.LS_INFO -> Timber.i("[onLogMessage] label: $label, message: $message")
                            Logging.Severity.LS_WARNING -> Timber.w("[onLogMessage] label: $label, message: $message")
                            Logging.Severity.LS_ERROR -> Timber.e("[onLogMessage] label: $label, message: $message")
                            Logging.Severity.LS_NONE -> Timber.d("[onLogMessage] label: $label, message: $message")
                            else -> Unit
                        }
                    }, Logging.Severity.LS_VERBOSE)
                    .createInitializationOptions()
            )

            return PeerConnectionFactory
                .builder()
                .setVideoDecoderFactory(videoDecoderFactory)
                .setVideoEncoderFactory(videoEncoderFactory)
                .setAudioDeviceModule(
                    JavaAudioDeviceModule
                        .builder(context)
                        .setUseHardwareAcousticEchoCanceler(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        .setUseHardwareNoiseSuppressor(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        .setAudioRecordErrorCallback(object : JavaAudioDeviceModule.AudioRecordErrorCallback {
                            override fun onWebRtcAudioRecordInitError(errorMessage: String?) {
                                Timber.w("[onWebRtcAudioRecordInitError] $errorMessage")
                            }

                            override fun onWebRtcAudioRecordStartError(
                                errorCode: JavaAudioDeviceModule.AudioRecordStartErrorCode?,
                                errorMessage: String?
                            ) {
                                Timber.w("[onWebRtcAudioRecordInitError] $errorMessage")
                            }

                            override fun onWebRtcAudioRecordError(errorMessage: String?) {
                                Timber.w("[onWebRtcAudioRecordError] $errorMessage")
                            }
                        })
                        .setAudioTrackErrorCallback(object : JavaAudioDeviceModule.AudioTrackErrorCallback {
                            override fun onWebRtcAudioTrackInitError(errorMessage: String?) {
                                Timber.w("[onWebRtcAudioTrackInitError] $errorMessage")
                            }

                            override fun onWebRtcAudioTrackStartError(
                                errorCode: JavaAudioDeviceModule.AudioTrackStartErrorCode?,
                                errorMessage: String?
                            ) {
                                Timber.w("[onWebRtcAudioTrackStartError] $errorCode")
                            }

                            override fun onWebRtcAudioTrackError(errorMessage: String?) {
                                Timber.w("[onWebRtcAudioTrackError] $errorMessage")
                            }
                        })
                        .setAudioTrackStateCallback(object : AudioTrackStateCallback {
                            override fun onWebRtcAudioTrackStart() {
                                Timber.d("[onWebRtcAudioTrackStart] no args")
                            }

                            override fun onWebRtcAudioTrackStop() {
                                Timber.d("[onWebRtcAudioTrackStop] no args")
                            }
                        })
                        .createAudioDeviceModule().also {
                            it.setMicrophoneMute(false)
                            it.setSpeakerMute(false)
                        }
                )
                .createPeerConnectionFactory()

        }

    /**
     * Builds a [StreamPeerConnection] that wraps the WebRTC [PeerConnection] and exposes several
     * helpful handlers.
     *
     * @param coroutineScope Scope used for asynchronous operations.
     * @param configuration The [PeerConnection.RTCConfiguration] used to set up the connection.
     * @param type The type of connection, either a subscriber of a publisher.
     * @param mediaConstraints Constraints used for audio and video tracks in the connection.
     * @param onStreamAdded Handler when a new [MediaStream] gets added.
     * @param onNegotiationNeeded Handler when there's a new negotiation.
     * @param onIceCandidateRequest Handler whenever we receive [IceCandidate]s.
     * @return [StreamPeerConnection] That's fully set up and can be observed and used to send and
     * receive tracks.
     */
    fun makePeerConnection(
        coroutineScope: CoroutineScope,
        configuration: RTCConfiguration,
        type: StreamPeerType,
        mediaConstraints: MediaConstraints,
        onStreamAdded: ((MediaStream) -> Unit)? = null,
        onNegotiationNeeded: ((StreamPeerConnection, StreamPeerType) -> Unit)? = null,
        onIceCandidateRequest: ((IceCandidate, StreamPeerType) -> Unit)? = null,
        onVideoTrack: ((RtpTransceiver?) -> Unit)? = null
    ): StreamPeerConnection {
        val peerConnection = StreamPeerConnection(
            coroutineScope = coroutineScope,
            type = type,
            mediaConstraints = mediaConstraints,
            onStreamAdded = onStreamAdded,
            onNegotiationNeeded = onNegotiationNeeded,
            onIceCandidate = onIceCandidateRequest,
            onVideoTrack = onVideoTrack
        )
        val connection = makePeerConnectionInternal(
            configuration = configuration,
            observer = peerConnection
        )
        return peerConnection.apply { initialize(connection) }
    }

    /**
     * Builds a [PeerConnection] internally that connects to the server and is able to send and
     * receive tracks.
     *
     * @param configuration The [PeerConnection.RTCConfiguration] used to set up the connection.
     * @param observer Handler used to observe different states of the connection.
     * @return [PeerConnection] that's fully set up.
     */
    private fun makePeerConnectionInternal(
        configuration: RTCConfiguration,
        observer: Observer
    ): PeerConnection {
        return requireNotNull(
            factory.createPeerConnection(configuration, observer)
        )
    }

    /**
     * Builds a [VideoSource] from the [factory] that can be used for regular video share (camera)
     * or screen sharing.
     *
     * @param isScreenCast If we're screen sharing using this source.
     * @return [VideoSource] that can be used to build tracks.
     */
    fun makeVideoSource(isScreenCast: Boolean): VideoSource =
        factory.createVideoSource(isScreenCast)

    /**
     * Builds a [VideoTrack] from the [factory] that can be used for regular video share (camera)
     * or screen sharing.
     *
     * @param source The [VideoSource] used for the track.
     * @param trackId The unique ID for this track.
     * @return [VideoTrack] That represents a video feed.
     */
    fun makeVideoTrack(source: VideoSource, trackId: String): VideoTrack =
        factory.createVideoTrack(trackId, source)

    /**
     * Builds an [AudioSource] from the [factory] that can be used for audio sharing.
     *
     * @param constraints The constraints used to change the way the audio behaves.
     * @return [AudioSource] that can be used to build tracks.
     */
    fun makeAudioSource(constraints: MediaConstraints = MediaConstraints()): AudioSource =
        factory.createAudioSource(constraints)

    /**
     * Builds an [AudioTrack] from the [factory] that can be used for regular video share (camera)
     * or screen sharing.
     *
     * @param source The [AudioSource] used for the track.
     * @param trackId The unique ID for this track.
     * @return [AudioTrack] That represents an audio feed.
     */
    fun makeAudioTrack(source: AudioSource, trackId: String): AudioTrack =
        factory.createAudioTrack(trackId, source)

}