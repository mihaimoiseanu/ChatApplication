package com.kroncoders.android.ui.screens.call

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kroncoders.android.networking.call.CallService
import com.kroncoders.android.networking.call.CallState
import com.kroncoders.android.networking.webrtc.session.WebRtcSessionManager
import com.kroncoders.android.repository.ChatRepository
import com.kroncoders.android.ui.navigation.NavigationManager
import com.kroncoders.android.ui.navigation.directions.Call.Companion.getCalling
import com.kroncoders.android.ui.navigation.directions.Call.Companion.getConversationIdForCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallScreenViewModel @Inject constructor(
    private val callService: CallService,
    private val savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    val sessionManager: WebRtcSessionManager,
    private val navigationManager: NavigationManager
) : ViewModel() {

    private val conversationId = savedStateHandle.getConversationIdForCall()
    private val calling = savedStateHandle.getCalling()

    val callScreenModel: MutableStateFlow<CallScreenModel> = MutableStateFlow(CallScreenModel())

    val localStream by sessionManager::localVideoTrackStream
    val remoteStream by sessionManager::remoteVideoTrackStream

    init {
        callService
            .callState
            .onEach { state -> handleCallStateFromService(state) }
            .launchIn(viewModelScope)

        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.getConversation(conversationId).collect { conversation ->
                callScreenModel.emit(callScreenModel.value.copy(userName = conversation.users.first().userName))
            }
        }
        if (calling) {
            makeCall()
        }
    }

    private fun makeCall() {
        callService.makeCall(conversationId)
    }

    fun acceptCall(accept: Boolean) {
        callService.acceptCall(conversationId, acceptCall = accept)
    }

    fun endCall() {
        callService.endCall(conversationId)
    }

    fun micEnabled(enabled: Boolean) {
        callScreenModel.update { it.copy(isMicrophoneEnabled = enabled) }
        sessionManager.enableMicrophone(enabled)
    }

    fun cameraEnabled(enabled: Boolean) {
        callScreenModel.update { it.copy(isCameraEnabled = enabled) }
        sessionManager.enableCamera(enabled)
    }

    private fun handleCallStateFromService(state: CallState) {
        callScreenModel.update { currentModel ->
            when (state) {
                CallState.Busy -> currentModel.copy(state = CallScreenState.Busy)
                is CallState.Called -> currentModel.copy(state = CallScreenState.Called)
                is CallState.Calling -> currentModel.copy(state = CallScreenState.Calling)
                is CallState.Connecting -> currentModel.copy(state = CallScreenState.Connecting)
                CallState.End -> {
                    viewModelScope.launch { navigationManager.navigateBack() }
                    currentModel.copy(state = CallScreenState.Finished)
                }
                is CallState.InCall -> currentModel.copy(state = CallScreenState.InCall)
                CallState.Inactive -> currentModel
            }
        }
    }

}