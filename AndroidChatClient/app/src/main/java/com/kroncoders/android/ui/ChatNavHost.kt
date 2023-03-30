package com.kroncoders.android.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.kroncoders.android.ui.navigation.directions.*
import com.kroncoders.android.ui.screens.call.CallScreen
import com.kroncoders.android.ui.screens.chat.ChatScreen
import com.kroncoders.android.ui.screens.conversations.create.ConversationCreateScreen
import com.kroncoders.android.ui.screens.conversations.edit.ConversationEditScreen
import com.kroncoders.android.ui.screens.conversations.list.ConversationsListScreen
import com.kroncoders.android.ui.screens.loading.LoadingScreen
import com.kroncoders.android.ui.screens.login.LoginScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ChatNavHost(navController: NavHostController) {
    AnimatedNavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = Loading::class.toString(),
        enterTransition = { slideInHorizontally { fullWidth -> fullWidth } },
        exitTransition = { slideOutHorizontally { fullWidth -> -fullWidth / 2 } + fadeOut() },
        popEnterTransition = { slideInHorizontally { fullWidth -> -fullWidth } },
        popExitTransition = { slideOutHorizontally { fullWidth -> fullWidth } + fadeOut() }
    ) {
        composable(Loading::class.toString()) { LoadingScreen() }
        composable(Login::class.toString()) { LoginScreen() }
        composable(ConversationsList::class.toString()) { ConversationsListScreen() }
        composable(ConversationsCreate::class.toString()) { ConversationCreateScreen() }
        composable(Chat::class.toString()) { ChatScreen() }
        composable(ConversationEdit::class.toString()) { ConversationEditScreen() }
        composable(Call::class.toString()) { CallScreen() }
    }

}