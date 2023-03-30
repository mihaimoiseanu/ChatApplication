package com.kroncoders.android.ui.screens.call.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CalledControls(
    modifier: Modifier = Modifier,
    onAcceptCallClick: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            modifier = Modifier
                .clip(CircleShape)
                .size(64.dp)
                .background(Color.Green),
            onClick = { onAcceptCallClick(true) }
        ) {
            Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = Color.Black)
        }

        Spacer(modifier = Modifier.fillMaxWidth(0.4f))

        IconButton(
            modifier = Modifier
                .clip(CircleShape)
                .size(64.dp)
                .background(Color.Red),
            onClick = { onAcceptCallClick(false) }
        ) {
            Icon(imageVector = Icons.Default.CallEnd, contentDescription = null, tint = Color.Black)
        }
    }
}

@Preview
@Composable
fun CalledControlsPreview() {
    CalledControls(onAcceptCallClick = {})
}