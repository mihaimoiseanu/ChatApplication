package com.kroncoders.android.networking.webrtc.session

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.content.getSystemService
import com.kroncoders.android.networking.webrtc.audio.AudioHandler
import com.kroncoders.android.networking.webrtc.audio.AudioSwitchHandler
import com.kroncoders.android.networking.webrtc.peer.StreamPeerConnection
import com.kroncoders.android.networking.webrtc.peer.StreamPeerConnectionFactory
import com.kroncoders.android.networking.webrtc.peer.StreamPeerType
import com.kroncoders.android.networking.webrtc.utils.stringify
import dagger.hilt.android.qualifiers.ApplicationContext
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.webrtc.*
import java.util.*

private const val ICE_SEPARATOR = '$'

val LocalWebRtcSessionManager: ProvidableCompositionLocal<WebRtcSessionManagerImpl> =
    staticCompositionLocalOf { error("WebRtcSessionManager was not initialized") }

class WebRtcSessionManagerImpl(
    @ApplicationContext private val context: Context,
    override val peerConnectionFactory: StreamPeerConnectionFactory
) : WebRtcSessionManager {

    private val logger by taggedLogger("Call::LocalWebRtcSessionManager")
    private val sessionManagerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _iceCandidateStream: MutableSharedFlow<IceCandidate> = MutableSharedFlow()
    override val iceCandidateStream: SharedFlow<IceCandidate> = _iceCandidateStream

    // used to send local video track to the fragment
    private val _localVideoTrackStream: MutableSharedFlow<VideoTrack> = MutableSharedFlow()
    override val localVideoTrackStream: MutableSharedFlow<VideoTrack> = _localVideoTrackStream

    // used to send remote video track to the sender
    private val _remoteVideoTrackStream: MutableSharedFlow<VideoTrack> = MutableSharedFlow()
    override val remoteVideoTrackStream: MutableSharedFlow<VideoTrack> = MutableSharedFlow()

    // declaring video constraints and setting OfferToReceiveVideo to true
    // this step is mandatory to create valid offer and answer
    private val mediaConstraints = MediaConstraints().apply {
        mandatory.addAll(
            listOf(
                MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"),
                MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true")
            )
        )
    }

    // getting front camera
    private val videoCapturer: VideoCapturer by lazy { buildCameraCapturer() }
    private val cameraManager by lazy { context.getSystemService<CameraManager>() }
    private val cameraEnumerator: Camera2Enumerator by lazy { Camera2Enumerator(context) }

    private val resolution: CameraEnumerationAndroid.CaptureFormat
        get() {
            val frontCamera = cameraEnumerator.deviceNames.first { cameraName -> cameraEnumerator.isFrontFacing(cameraName) }
            val supportedFormats = cameraEnumerator.getSupportedFormats(frontCamera) ?: emptyList()
            return supportedFormats.firstOrNull {
                (it.width == 720 || it.width == 480 || it.width == 360)
            } ?: error("There is no matched resolution")
        }

    // we need it to initialize video capturer
    private val surfaceTextureHelper = SurfaceTextureHelper.create("SurfaceTextureHelperThread", peerConnectionFactory.eglBaseContext)

    private val videoSource by lazy {
        peerConnectionFactory.makeVideoSource(videoCapturer.isScreencast).apply {
            videoCapturer.initialize(surfaceTextureHelper, context, this.capturerObserver)
            videoCapturer.startCapture(resolution.width, resolution.height, 30)
        }
    }
    private val localVideoTrack: VideoTrack by lazy {
        peerConnectionFactory.makeVideoTrack(
            source = videoSource,
            trackId = "Video${UUID.randomUUID()}"
        )
    }

    /** Audio properties */

    private val audioHandler: AudioHandler by lazy { AudioSwitchHandler(context) }
    private val audioManager by lazy { context.getSystemService<AudioManager>() }
    private val audioConstraints: MediaConstraints by lazy { buildMediaConstraints() }
    private val audioSource by lazy { peerConnectionFactory.makeAudioSource(audioConstraints) }
    private val localAudioTrack: AudioTrack by lazy { peerConnectionFactory.makeAudioTrack(audioSource, "Audio${UUID.randomUUID()}") }

    private var _peerConnection: StreamPeerConnection? = null
    private val peerConnection: StreamPeerConnection
        get() = _peerConnection ?: throw IllegalStateException("There is no connection")

    override fun createSession() {
        setupAudio()
        _peerConnection = buildPeerConnection()
        peerConnection.connection.addTrack(localVideoTrack)
        peerConnection.connection.addTrack(localAudioTrack)
        sessionManagerScope.launch { _localVideoTrackStream.emit(localVideoTrack) }
    }

    override suspend fun getAnswerToOffer(sdp: String): String {
        peerConnection.setRemoteDescription(
            SessionDescription(SessionDescription.Type.OFFER, sdp)
        )
        val answer = peerConnection.createAnswer().getOrThrow()
        peerConnection.setLocalDescription(answer).getOrThrow()
        logger.d { "[SDP] send answer: ${answer.stringify()}" }
        return answer.description
    }

    override suspend fun getOffer(): String {
        val offer = peerConnection.createOffer().getOrThrow()
        peerConnection.setLocalDescription(offer).getOrThrow()
        logger.d { "[SDP] send offer: ${offer.stringify()}" }
        return offer.description
    }

    override suspend fun handleAnswer(sdp: String) {
        logger.d { "[SDP] handle answer: $sdp" }
        peerConnection.setRemoteDescription(
            SessionDescription(SessionDescription.Type.ANSWER, sdp)
        )
    }

    override suspend fun handleIce(iceMessage: String) {
        val iceArray = iceMessage.split(ICE_SEPARATOR)
        peerConnection.addIceCandidate(
            IceCandidate(
                iceArray[0],
                iceArray[1].toInt(),
                iceArray[2]
            )
        )
    }

    override fun flipCamera() {
        (videoCapturer as? Camera2Capturer)?.switchCamera(null)
    }

    override fun enableMicrophone(enabled: Boolean) {
        audioManager?.isMicrophoneMute = enabled
    }

    override fun enableCamera(enabled: Boolean) {
        if (enabled) {
            videoCapturer.startCapture(resolution.width, resolution.height, 30)
        } else {
            videoCapturer.stopCapture()
        }
    }

    override fun disconnect() {
        //dispose audio & video tracks
        remoteVideoTrackStream.replayCache.forEach { videoTrack -> videoTrack.dispose() }
        localVideoTrackStream.replayCache.forEach { videoTrack -> videoTrack.dispose() }
        localAudioTrack.dispose()
        localVideoTrack.dispose()

        //dispose audio handler and video capturer
        audioHandler.stop()
        videoCapturer.stopCapture()
        videoCapturer.dispose()

        //dispose the peer connection
        peerConnection.disposeConnection()
        _peerConnection = null
    }

    private fun buildPeerConnection(): StreamPeerConnection {
        return peerConnectionFactory.makePeerConnection(
            coroutineScope = sessionManagerScope,
            configuration = peerConnectionFactory.rtcConfig,
            type = StreamPeerType.SUBSCRIBER,
            mediaConstraints = mediaConstraints,
            onIceCandidateRequest = { iceCandidate, _ -> sessionManagerScope.launch { _iceCandidateStream.emit(iceCandidate) } },
            onVideoTrack = { rtpTransceiver ->
                val track = rtpTransceiver?.receiver?.track() ?: return@makePeerConnection
                if (track.kind() == MediaStreamTrack.VIDEO_TRACK_KIND) {
                    val videoTrack = track as VideoTrack
                    sessionManagerScope.launch { _remoteVideoTrackStream.emit(videoTrack) }
                }
            }
        )
    }

    private fun buildCameraCapturer(): VideoCapturer {
        val manager = cameraManager ?: throw RuntimeException("CameraManager not initialized")

        val ids = manager.cameraIdList

        var cameraId = ids.firstOrNull { id ->
            val characteristics = manager.getCameraCharacteristics(id)
            val cameraLensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            cameraLensFacing == CameraMetadata.LENS_FACING_FRONT
        } ?: ""

        if (cameraId.isBlank() && ids.isNotEmpty()) {
            cameraId = ids.first()
        }
        return Camera2Capturer(context, cameraId, null)
    }

    private fun buildMediaConstraints(): MediaConstraints {
        val mediaConstraints = MediaConstraints()
        val items = listOf(
            MediaConstraints.KeyValuePair(
                "googEchoCancellation",
                true.toString()
            ),
            MediaConstraints.KeyValuePair(
                "googAutoGainControl",
                true.toString()
            ),
            MediaConstraints.KeyValuePair(
                "googHighpassFilter",
                true.toString()
            ),
            MediaConstraints.KeyValuePair(
                "googNoiseSuppression",
                true.toString()
            ),
            MediaConstraints.KeyValuePair(
                "googTypingNoiseDetection",
                true.toString()
            )
        )
        return mediaConstraints.apply {
            with(optional) {
                add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
                addAll(items)
            }
        }
    }

    private fun setupAudio() {
        logger.d { "[setupAudio] #sfu; no args" }
        audioHandler.start()
        audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val devices = audioManager?.availableCommunicationDevices ?: return
            val deviceType = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER

            val device = devices.firstOrNull { it.type == deviceType } ?: return

            val isCommunicationDeviceSet = audioManager?.setCommunicationDevice(device)
            logger.d { "[setupAudio] #sfu; isCommunicationDeviceSet: $isCommunicationDeviceSet" }
        }
    }
}