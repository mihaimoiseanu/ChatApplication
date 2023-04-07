package com.kroncoders.android.ui.screens.conversations.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ConversationsListScreen(viewModel: ConversationsListViewModel = hiltViewModel()) {

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = { Text("Conversations") },
                actions = {
                    IconButton(onClick = viewModel::logout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::goToCreateConversation) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null
                )
            }
        }) { paddingValues ->

        val screenModel by viewModel.screenModel.collectAsState()

        val refreshState = rememberPullRefreshState(
            refreshing = screenModel.isLoading,
            onRefresh = viewModel::refreshConversations
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(refreshState)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                if (!screenModel.isLoading) {
                    items(screenModel.conversations.size) { index ->
                        val conversation = screenModel.conversations[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clickable { viewModel.openConversation(conversation) }
                                .padding(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .height(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                            )

                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = conversation.name,
                                    fontSize = 16.sp
                                )

                                val messageToDisplay = conversation.lastMessage.takeIf { it.isNotBlank() } ?: "Start sending messages"
                                Text(
                                    modifier = Modifier.padding(top = 2.dp),
                                    text = messageToDisplay,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        if (index < screenModel.conversations.size - 1) {
                            Divider()
                        }
                    }
                }
            }

            PullRefreshIndicator(
                modifier = Modifier.align(Alignment.TopCenter),
                refreshing = screenModel.isLoading,
                state = refreshState
            )
        }
    }
}