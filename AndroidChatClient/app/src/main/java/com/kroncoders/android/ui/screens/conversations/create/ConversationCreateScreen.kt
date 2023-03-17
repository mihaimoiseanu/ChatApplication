package com.kroncoders.android.ui.screens.conversations.create

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kroncoders.android.repository.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationCreateScreen(viewModel: ConversationCreateViewModel = hiltViewModel()) {


    val screenModel by viewModel.screenModel.collectAsState()

    BackHandler(onBack = viewModel::navigateBack)

    Scaffold(
        modifier = Modifier
            .systemBarsPadding()
            .imePadding()
            .fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = if (screenModel.multiSelect) "Select users" else "Users")
                },
                navigationIcon = {
                    if (screenModel.multiSelect) {
                        IconButton(onClick = viewModel::cancelMultiSelect) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                        }
                    } else {
                        IconButton(onClick = viewModel::navigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIosNew,
                                contentDescription = null
                            )
                        }
                    }
                }
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                if (screenModel.users.isNotEmpty()) {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        items(screenModel.users.size) { index ->
                            val user = screenModel.users[index]
                            UserItem(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .fillMaxWidth(),
                                user = user,
                                isSelected = screenModel.selectedUsers.contains(user) && screenModel.multiSelect,
                                onClick = viewModel::onUserClicked,
                                onLongClick = viewModel::onUserLongClicked
                            )
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

            if (screenModel.multiSelect) {
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

    if (screenModel.error.isNotBlank()) {
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            confirmButton = {
                TextButton(onClick = viewModel::clearError) {
                    Text("Ok")
                }
            },
            text = { Text(text = screenModel.error) }
        )
    }
}

@Composable
fun UserItem(
    modifier: Modifier = Modifier,
    user: User,
    isSelected: Boolean,
    onClick: (User) -> Unit,
    onLongClick: (User) -> Unit
) {
    val userIconColor = user.hashCode() % 256
    val iconColor =
        if (isSelected)
            Color.Green
        else
            Color(userIconColor, userIconColor, userIconColor)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick(user) },
                    onLongPress = { onLongClick(user) }
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconColor),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White
                )
            } else {
                Text(text = user.userName[0].toString(), color = Color.White)
            }
        }
        Text(
            modifier = Modifier.padding(start = 10.dp),
            text = user.userName
        )
    }
}