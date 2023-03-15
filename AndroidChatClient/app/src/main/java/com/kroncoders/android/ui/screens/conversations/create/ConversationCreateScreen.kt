package com.kroncoders.android.ui.screens.conversations.create

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationCreateScreen(viewModel: ConversationCreateViewModel = hiltViewModel()) {


    val screenModel by viewModel.screenModel.collectAsState()

    BackHandler(onBack = viewModel::navigateBack)

    Scaffold(modifier = Modifier
        .systemBarsPadding()
        .imePadding()
        .fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Create conversation")
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {

                OutlinedTextField(
                    modifier = Modifier
                        .padding(start = 20.dp, top = 20.dp, end = 20.dp)
                        .fillMaxWidth(),
                    value = screenModel.name,
                    onValueChange = viewModel::onNameChanged,
                    label = { Text(text = "Name") }
                )

                if (screenModel.users.isNotEmpty()) {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
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
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "There are no user :(\nYou can talk by yourself",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            ElevatedButton(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                onClick = viewModel::createChat
            ) {
                Text(text = "Create chat")
            }
        }

    }

}