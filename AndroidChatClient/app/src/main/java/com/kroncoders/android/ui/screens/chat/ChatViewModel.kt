package com.kroncoders.android.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kroncoders.android.repository.ChatRepository
import com.kroncoders.android.repository.models.Message
import com.kroncoders.android.ui.navigation.NavigationManager
import com.kroncoders.android.ui.navigation.directions.Chat.Companion.getConversationId
import com.kroncoders.android.ui.navigation.directions.ConversationEdit
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.log.Priority
import io.getstream.log.streamLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ChatScreenModel(
    val textInput: String = "",
    val messages: List<Message> = emptyList(),
    val currentUserID: Long = -1L,
    val conversationId: Long = -1L,
    val conversationName: String = "",
    val error: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
    private val savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val screenModel = MutableStateFlow(ChatScreenModel())

    init {
        getCurrentUser()
        val conversationId = savedStateHandle.getConversationId()
        screenModel.update { it.copy(conversationId = conversationId) }
        syncConversation(conversationId)
        getMessages(conversationId)
    }

    fun onInputChanged(value: String) {
        screenModel.update { it.copy(textInput = value) }
    }

    fun onSendMessage() = viewModelScope.launch(Dispatchers.IO) {
        val screenModel = screenModel.value
        val networkMessage = Message(
            id = UUID.randomUUID().toString(),
            text = screenModel.textInput.trim(),
            sentTime = System.currentTimeMillis(),
            userId = screenModel.currentUserID,
            conversationId = screenModel.conversationId
        )
        chatRepository.sendMessage(networkMessage)
        this@ChatViewModel.screenModel.update { it.copy(textInput = "") }
    }

    fun onBackClick() {
        viewModelScope.launch { navigationManager.navigateBack() }
    }

    fun clearError() {
        screenModel.update { it.copy(error = "") }
    }

    fun addUsers() {
        viewModelScope.launch { navigationManager.navigate(ConversationEdit(screenModel.value.conversationId)) }
    }

    private fun getCurrentUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserID = chatRepository.currentUserId()
            screenModel.update { it.copy(currentUserID = currentUserID) }
        }
    }

    private fun getMessages(conversationId: Long) {
        chatRepository
            .getMessagesForConversation(conversationId)
            .onEach { messages -> screenModel.update { it.copy(messages = messages) } }
            .launchIn(viewModelScope)
    }

    private fun syncConversation(conversationId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                chatRepository.syncConversation(conversationId)
            } catch (exception: Exception) {
                streamLog(priority = Priority.ERROR, throwable = exception) { "Error in sync conversations " }
            }
        }
        chatRepository
            .getConversation(conversationId = conversationId)
            .onEach { conversation -> screenModel.update { it.copy(conversationName = conversation.name) } }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }
}