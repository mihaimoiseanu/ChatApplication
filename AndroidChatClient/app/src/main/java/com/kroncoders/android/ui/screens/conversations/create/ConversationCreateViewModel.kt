package com.kroncoders.android.ui.screens.conversations.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kroncoders.android.repository.ChatRepository
import com.kroncoders.android.repository.models.Conversation
import com.kroncoders.android.repository.models.User
import com.kroncoders.android.ui.executeRequest
import com.kroncoders.android.ui.navigation.NavigationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationCreateScreenModel(
    val name: String = "",
    val users: List<User> = emptyList(),
    val selectedUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = ""
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

    fun getUsers() {
        viewModelScope.launch(Dispatchers.IO) { chatRepository.syncUsers() }
        chatRepository
            .getAllUsers()
            .onEach { users ->
                val currentUser = chatRepository.currentUserId()
                screenModel.update { it.copy(users = users.filter { it.id != currentUser }) }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    fun onNameChanged(value: String) {
        screenModel.update { it.copy(name = value) }
    }

    fun onUserCheckedChanged(isChecked: Boolean, user: User) {
        val users = screenModel
            .value
            .selectedUsers
            .let { if (isChecked) it + user else it - user }
        screenModel.update { it.copy(selectedUsers = users) }
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

    fun navigateBack() {
        viewModelScope.launch { navigationManager.navigateBack() }
    }
}