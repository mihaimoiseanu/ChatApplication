package com.kroncoders.android.ui.screens.conversations.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationEditScreen(viewModel: ConversationEditViewModel = hiltViewModel()) {

    val screenModel by viewModel.screenModel.collectAsState()

    Scaffold(
        modifier = Modifier
            .systemBarsPadding()
            .imePadding()
            .fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier,
                title = { Text("Edit Conversation") },
                navigationIcon = {
                    IconButton(onClick = viewModel::goBack) {
                        Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = null)
                    }
                }
            )
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 10.dp)
                    .fillMaxWidth(),
                value = screenModel.name,
                onValueChange = viewModel::onConversationNameChanged,
                label = { Text("Conversation name") }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                items(screenModel.users.size) { index ->
                    val user = screenModel.users[index]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = user.userName)
                        Spacer(modifier = Modifier.weight(1f))
                        Checkbox(
                            checked = screenModel.selectedUsers.contains(user),
                            onCheckedChange = { viewModel.onUserCheckedChanged(it, user) })
                    }
                    if (index < screenModel.users.size - 1) {
                        Divider()
                    }
                }
            }

            ElevatedButton(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                onClick = viewModel::updateChat
            ) {
                Text(text = "Update chat")
            }
        }
    }

}