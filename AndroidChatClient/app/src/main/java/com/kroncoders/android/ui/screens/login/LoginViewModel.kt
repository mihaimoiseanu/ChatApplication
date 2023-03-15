package com.kroncoders.android.ui.screens.login

import androidx.lifecycle.ViewModel
import com.kroncoders.android.repository.ChatRepository
import com.kroncoders.android.repository.models.User
import com.kroncoders.android.ui.executeRequest
import com.kroncoders.android.ui.navigation.NavigationManager
import com.kroncoders.android.ui.navigation.directions.ConversationsList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class LoginScreenModel(
    val userName: String = "",
    val error: String = "",
    val isLoading: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val screenModel = MutableStateFlow(LoginScreenModel())

    fun onUserNameChanged(value: String) {
        if (screenModel.value.isLoading) return
        screenModel.value = screenModel.value.copy(userName = value)
    }

    fun clearError() {
        screenModel.value = screenModel.value.copy(error = "")
    }

    fun onLoginClicked() {
        executeRequest(
            block = { chatRepository.loginUser(User(userName = screenModel.value.userName)) },
            onLoading = { loading -> screenModel.update { it.copy(isLoading = loading) } },
            onError = { error -> screenModel.update { it.copy(error = error) } },
            onSuccess = {
                chatRepository.connectToServer()
                navigationManager.navigate(ConversationsList)
            }
        )
    }
}