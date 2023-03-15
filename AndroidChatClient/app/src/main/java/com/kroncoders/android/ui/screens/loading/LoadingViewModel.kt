package com.kroncoders.android.ui.screens.loading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kroncoders.android.repository.ChatRepository
import com.kroncoders.android.ui.navigation.NavigationManager
import com.kroncoders.android.ui.navigation.directions.ConversationsList
import com.kroncoders.android.ui.navigation.directions.Login
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoadingViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val navigationManager: NavigationManager
) : ViewModel() {


    init {
        viewModelScope.launch(Dispatchers.IO) {
            delay(2000L)
            val currentUser = chatRepository.currentUserId()
            if (currentUser == -1L) {
                navigationManager.navigate(Login)
            } else {
                chatRepository.connectToServer()
                navigationManager.navigate(ConversationsList)
            }
        }
    }
}