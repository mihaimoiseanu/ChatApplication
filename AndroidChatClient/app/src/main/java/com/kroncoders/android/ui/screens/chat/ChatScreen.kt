package com.kroncoders.android.ui.screens.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kroncoders.android.repository.models.Message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {

    val screenModel by viewModel.screenModel.collectAsState()

    BackHandler(onBack = viewModel::onBackClick)

    Column(
        modifier = Modifier
            .systemBarsPadding()
            .imePadding()
            .fillMaxSize()
    ) {

        CenterAlignedTopAppBar(
            modifier = Modifier.fillMaxWidth(),
            title = { Text(screenModel.conversationName) },
            actions = {
                IconButton(onClick = viewModel::addUsers) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = viewModel::onBackClick) {
                    Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = null)
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            reverseLayout = true,
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 10.dp)
        ) {
            items(screenModel.messages.size) { index ->
                val message = screenModel.messages[index]
                if (message.userId == screenModel.currentUserID) {
                    OutgoingMessage(
                        modifier = Modifier.padding(top = 2.dp),
                        message = message
                    )
                } else {
                    IncomingMessage(
                        modifier = Modifier.padding(top = 2.dp),
                        message = message
                    )
                }
            }
        }

        MessageInput(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            value = screenModel.textInput,
            onTextChanged = viewModel::onInputChanged,
            onSendButtonClicked = viewModel::onSendMessage
        )
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
fun IncomingMessage(
    modifier: Modifier = Modifier,
    message: Message
) {

    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {

        val maxWidth = LocalConfiguration.current.screenWidthDp.dp / 2

        Text(
            modifier = Modifier
                .widthIn(0.dp, maxWidth)
                .clip(RoundedCornerShape(5.dp, 5.dp, 5.dp, 0.dp))
                .background(Color.Gray)
                .padding(5.dp),
            text = message.text,
            color = Color.White
        )

    }

}

@Composable
fun OutgoingMessage(
    modifier: Modifier = Modifier,
    message: Message
) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {

        val maxWidth = LocalConfiguration.current.screenWidthDp.dp / 1.8f

        Text(
            modifier = Modifier
                .widthIn(0.dp, maxWidth)
                .clip(RoundedCornerShape(5.dp, 5.dp, 0.dp, 5.dp))
                .background(Color.Blue)
                .padding(5.dp),
            text = message.text,
            color = Color.White
        )

    }
}

@Preview
@Composable
fun MessagePreview() {
    Column {
        IncomingMessage(message = Message("", "teasdasdasdxt", 1L, 1L, 1L))
        OutgoingMessage(message = Message("", "tasdasdsadext", 1L, 1L, 1L))

        MessageInput(
            modifier = Modifier.height(52.dp),
            value = "",
            onTextChanged = {},
            onSendButtonClicked = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInput(
    modifier: Modifier = Modifier,
    value: String,
    onTextChanged: (String) -> Unit,
    onSendButtonClicked: () -> Unit
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.DarkGray),
        verticalAlignment = Alignment.CenterVertically
    ) {

        TextField(
            modifier = Modifier.weight(1f),
            value = value,
            onValueChange = onTextChanged,
            colors = TextFieldDefaults.textFieldColors(
                textColor = Color.White,
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            placeholder = { Text(text = "Aa", color = Color.White, fontSize = 14.sp) },
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            maxLines = 1
        )

        IconButton(
            onClick = onSendButtonClicked,
            enabled = value.isNotBlank()
        ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = null)
        }

    }

}