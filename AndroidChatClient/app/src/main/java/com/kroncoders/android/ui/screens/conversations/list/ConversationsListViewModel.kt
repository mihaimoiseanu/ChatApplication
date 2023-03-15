package com.kroncoders.android.ui.screens.conversations.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kroncoders.android.repository.ChatRepository
import com.kroncoders.android.repository.models.Conversation
import com.kroncoders.android.ui.executeRequest
import com.kroncoders.android.ui.navigation.NavigationManager
import com.kroncoders.android.ui.navigation.directions.Chat
import com.kroncoders.android.ui.navigation.directions.ConversationsCreate
import com.kroncoders.android.ui.navigation.directions.Loading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationsListScreenModel(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = ""
)

@HiltViewModel
class ConversationsListViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
    private val chatRepository: ChatRepository,
) : ViewModel() {

    val screenModel = MutableStateFlow(ConversationsListScreenModel())

    init {
        refreshConversations()
        getConversations()
    }

    fun refreshConversations() {
        executeRequest(
            block = { chatRepository.syncConversations() },
            onLoading = { isLoading -> screenModel.update { it.copy(isLoading = isLoading) } }
        )
    }

    fun getConversations() {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository
                .getConversations()
                .collect { conversations ->
                    screenModel.update { it.copy(conversations = conversations) }
                }
        }
    }

    fun openConversation(conversation: Conversation) {
        val conversationId = conversation.id
        viewModelScope.launch {
            navigationManager.navigate(Chat(conversationId))
        }
    }

    fun goToCreateConversation() = viewModelScope.launch {
        navigationManager.navigate(ConversationsCreate)
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.logout()
            navigationManager.navigate(Loading)
        }
    }
}