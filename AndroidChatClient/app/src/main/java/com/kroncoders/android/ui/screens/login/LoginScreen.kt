package com.kroncoders.android.ui.screens.login

import androidx.compose.foundation.layout.*
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
fun LoginScreen(loginViewModel: LoginViewModel = hiltViewModel()) {

    val screenModel by loginViewModel.screenModel.collectAsState()

    Column(
        modifier = Modifier
            .systemBarsPadding()
            .imePadding()
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = screenModel.userName,
            onValueChange = loginViewModel::onUserNameChanged,
            label = { Text(text = "UserName") }
        )

        ElevatedButton(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth(),
            onClick = loginViewModel::onLoginClicked
        ) {
            Text(text = "Login")
        }
    }

    if (screenModel.error.isNotBlank()) {
        AlertDialog(
            onDismissRequest = loginViewModel::clearError,
            confirmButton = {
                TextButton(onClick = loginViewModel::clearError) {
                    Text("Ok")
                }
            },
            text = { Text(text = screenModel.error) }
        )
    }

}