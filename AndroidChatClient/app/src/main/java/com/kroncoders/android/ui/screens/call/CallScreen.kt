package com.kroncoders.android.ui.screens.call

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kroncoders.android.networking.webrtc.session.LocalWebRtcSessionManager
import com.kroncoders.android.ui.components.FloatingVideoRenderer
import com.kroncoders.android.ui.components.VideoRenderer
import com.kroncoders.android.ui.screens.call.components.CalledControls
import com.kroncoders.android.ui.screens.call.components.CallingControls
import com.kroncoders.android.ui.screens.call.components.InCallControls

@Composable
fun CallScreen(viewModel: CallScreenViewModel = hiltViewModel()) {

    val screenModel by viewModel.callScreenModel.collectAsState()

    CompositionLocalProvider(LocalWebRtcSessionManager provides viewModel.sessionManager) {
        Box(
            modifier = Modifier
                .systemBarsPadding()
                .fillMaxSize()
        ) {

            var parentSize: IntSize by remember { mutableStateOf(IntSize(0, 0)) }

            val remoteVideoTrackState by viewModel.remoteStream.collectAsState(initial = null)
            val remoteVideoTrack = remoteVideoTrackState

            val localVideoTrackState by viewModel.localStream.collectAsState(initial = null)
            val localVideoTrack = localVideoTrackState

            if (remoteVideoTrack != null) {
                VideoRenderer(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { parentSize = it },
                    videoTrack = remoteVideoTrack
                )
            }

            if (localVideoTrack != null && screenModel.isCameraEnabled) {
                FloatingVideoRenderer(
                    modifier = Modifier
                        .size(width = 150.dp, height = 210.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .align(Alignment.TopEnd),
                    videoTrack = localVideoTrack,
                    parentBounds = parentSize,
                    paddingValues = PaddingValues(0.dp)
                )
            }

            if (screenModel.userName.isNotBlank()) {
                val callText = when (screenModel.state) {
                    CallScreenState.Calling -> "Calling ${screenModel.userName}"
                    CallScreenState.Called -> "${screenModel.userName} is calling"
                    CallScreenState.Busy -> "${screenModel.userName} is busy"
                    else -> screenModel.userName
                }
                Text(
                    modifier = Modifier
                        .padding(top = 48.dp)
                        .align(Alignment.TopCenter),
                    text = callText,
                    fontSize = 24.sp
                )
            }

            when (screenModel.state) {
                CallScreenState.Calling -> CallingControls(
                    modifier = Modifier
                        .padding(bottom = 40.dp)
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    onEndCallClicked = viewModel::endCall
                )
                CallScreenState.Called -> CalledControls(
                    modifier = Modifier
                        .padding(bottom = 40.dp)
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    onAcceptCallClick = viewModel::acceptCall
                )
                CallScreenState.InCall, CallScreenState.Connecting -> InCallControls(
                    modifier = Modifier
                        .padding(bottom = 40.dp)
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    isMicEnabled = screenModel.isMicrophoneEnabled,
                    isCameraEnabled = screenModel.isCameraEnabled,
                    onEndCallClicked = viewModel::endCall,
                    onCloseCameraClicked = viewModel::cameraEnabled,
                    onMuteClicked = viewModel::micEnabled
                )
                else -> {}
            }
        }
    }
}