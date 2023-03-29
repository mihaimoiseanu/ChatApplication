package com.kroncoders.android.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.kroncoders.android.networking.webrtc.session.LocalWebRtcSessionManager
import org.webrtc.RendererCommon
import org.webrtc.VideoTrack

/**
 * Renders a single video track based on the call state.
 *
 * @param videoTrack The track containing the video stream for a given participant.
 * @param modifier Modifier for styling.
 */

@Composable
fun VideoRenderer(
    modifier: Modifier = Modifier,
    videoTrack: VideoTrack
) {

    val trackState: MutableState<VideoTrack?> = remember { mutableStateOf(null) }
    var view: VideoTextureViewRenderer? by remember { mutableStateOf(null) }

    DisposableEffect(key1 = videoTrack) {
        onDispose {
            cleanTrack(view, trackState)
        }
    }

    val sessionManager = LocalWebRtcSessionManager.current
    AndroidView(
        factory = { context ->
            VideoTextureViewRenderer(context).apply {
                init(
                    sessionManager.peerConnectionFactory.eglBaseContext,
                    object : RendererCommon.RendererEvents {
                        override fun onFirstFrameRendered() = Unit
                        override fun onFrameResolutionChanged(videoWidth: Int, videoHeight: Int, rotation: Int) = Unit
                    }
                )
                setupVideo(trackState, videoTrack, this)
                view = this
            }
        },
        update = { v -> setupVideo(trackState, videoTrack, v) },
        modifier = modifier
    )
}

private fun cleanTrack(view: VideoTextureViewRenderer?, trackState: MutableState<VideoTrack?>) {
    view?.let { trackState.value?.removeSink(it) }
    trackState.value = null
}

private fun setupVideo(
    trackState: MutableState<VideoTrack?>,
    track: VideoTrack,
    renderer: VideoTextureViewRenderer
) {
    if (trackState.value == track) return

    cleanTrack(renderer, trackState)

    trackState.value = track
    track.addSink(renderer)
}