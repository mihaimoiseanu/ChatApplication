package com.kroncoders.android.ui.screens.conversations.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kroncoders.android.repository.ChatRepository
import com.kroncoders.android.repository.models.Conversation
import com.kroncoders.android.repository.models.User
import com.kroncoders.android.ui.executeRequest
import com.kroncoders.android.ui.navigation.NavigationManager
import com.kroncoders.android.ui.navigation.directions.ConversationEdit.Companion.getConversationId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationEditScreenModel(
    val id: Long = -1L,
    val name: String = "",
    val users: List<User> = emptyList(),
    val selectedUsers: List<User> = emptyList(),
    val error: String = ""
)

@HiltViewModel
class ConversationEditViewModel
@Inject constructor(
    private val chatRepository: ChatRepository,
    private val savedStateHandle: SavedStateHandle,
    private val navigationManager: NavigationManager
) : ViewModel() {

    val screenModel: MutableStateFlow<ConversationEditScreenModel> =
        MutableStateFlow(ConversationEditScreenModel())

    init {
        val conversationId = savedStateHandle.getConversationId()
        screenModel.update { it.copy(id = conversationId) }
        retrieveAllUsers()
        retrieveConversation(conversationId)
    }

    fun goBack() {
        viewModelScope.launch { navigationManager.navigateBack() }
    }

    fun onConversationNameChanged(value: String) {
        screenModel.update { it.copy(name = value) }
    }

    fun updateChat() {
        executeRequest(
            block = {
                val conversation = Conversation(
                    id = screenModel.value.id,
                    users = screenModel.value.selectedUsers,
                    lastUpdateTime = System.currentTimeMillis(),
                    name = screenModel.value.name
                )
                chatRepository.updateConversation(conversation)
            },
            onError = { error -> screenModel.update { it.copy(error = error) } },
            onSuccess = { navigationManager.navigateBack() },
        )
    }

    fun onUserCheckedChanged(isChecked: Boolean, user: User) {
        val users = screenModel
            .value
            .selectedUsers
            .let { if (isChecked) it + user else it - user }
        screenModel.update { it.copy(selectedUsers = users) }
    }

    private fun retrieveConversation(conversationId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository
                .getConversation(conversationId = conversationId)
                .collect { conversation ->
                    screenModel.update {
                        it.copy(
                            name = conversation.name,
                            selectedUsers = conversation.users,
                        )
                    }
                }
        }
    }

    private fun retrieveAllUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.getAllUsers().collect { users ->
                screenModel.update { it.copy(users = users) }
            }
        }
    }

}