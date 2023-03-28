package com.kroncoders.android.ui.screens.conversations.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kroncoders.android.repository.ChatRepository
import com.kroncoders.android.repository.models.Conversation
import com.kroncoders.android.repository.models.User
import com.kroncoders.android.ui.executeRequest
import com.kroncoders.android.ui.navigation.NavigationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.log.Priority
import io.getstream.log.streamLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationCreateScreenModel(
    val name: String = "",
    val users: List<User> = emptyList(),
    val selectedUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = "",
    val multiSelect: Boolean = false,
)

@HiltViewModel
class ConversationCreateViewModel
@Inject constructor(
    private val navigationManager: NavigationManager,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val screenModel = MutableStateFlow(ConversationCreateScreenModel())

    init {
        getUsers()
    }

    private fun getUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                chatRepository.syncUsers()
            } catch (exception: Exception) {
                streamLog(priority = Priority.ERROR, throwable = exception) { "Error in sync users " }
            }
        }
        chatRepository
            .getAllUsers()
            .onEach { users ->
                val currentUser = chatRepository.currentUserId()
                screenModel.update { it.copy(users = users.filter { user -> user.id != currentUser }) }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    fun cancelMultiSelect() {
        screenModel.update { it.copy(selectedUsers = emptyList(), multiSelect = false) }
    }

    fun onUserClicked(user: User) {
        if (screenModel.value.multiSelect) {
            onUserLongClicked(user)
        } else {
            screenModel.update { it.copy(selectedUsers = listOf(user)) }
            createChat()
        }
    }

    fun onUserLongClicked(user: User) {
        val currentSelectedUsers = screenModel.value.selectedUsers
        val selectedUsers = if (currentSelectedUsers.contains(user))
            currentSelectedUsers - user
        else
            currentSelectedUsers + user
        screenModel.update { it.copy(multiSelect = true, selectedUsers = selectedUsers) }
    }

    fun createChat() {
        executeRequest(
            block = {
                val conversation = Conversation(
                    name = screenModel.value.name,
                    users = screenModel.value.selectedUsers,
                )
                chatRepository.createConversation(conversation = conversation)
            },
            onError = { error -> screenModel.update { it.copy(error = error) } },
            onLoading = { isLoading -> screenModel.update { it.copy(isLoading = isLoading) } },
            onSuccess = { navigationManager.navigateBack() }
        )
    }

    fun clearError() {
        screenModel.update { it.copy(error = "") }
    }

    fun navigateBack() {
        viewModelScope.launch { navigationManager.navigateBack() }
    }
}