package com.kroncoders.android.ui.screens.call.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun InCallControls(
    modifier: Modifier = Modifier,
    isMicEnabled: Boolean = true,
    isCameraEnabled: Boolean = true,
    onEndCallClicked: () -> Unit,
    onCloseCameraClicked: (Boolean) -> Unit,
    onMuteClicked: (Boolean) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            modifier = Modifier
                .clip(CircleShape)
                .size(48.dp)
                .background(Color.White),
            onClick = { onMuteClicked(!isMicEnabled) }
        ) {
            Icon(imageVector = if (isMicEnabled) Icons.Default.Mic else Icons.Default.MicOff, contentDescription = null, tint = Color.Black)
        }

        IconButton(
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .clip(CircleShape)
                .size(64.dp)
                .background(Color.Red),
            onClick = onEndCallClicked
        ) {
            Icon(imageVector = Icons.Default.CallEnd, contentDescription = null, tint = Color.Black)
        }

        IconButton(
            modifier = Modifier
                .clip(CircleShape)
                .size(48.dp)
                .background(Color.White),
            onClick = { onCloseCameraClicked(!isCameraEnabled) }
        ) {
            Icon(
                imageVector = if (isCameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                contentDescription = null,
                tint = Color.Black
            )
        }

    }
}

@Preview
@Composable
fun InCallControlsPreview() {
    InCallControls(onEndCallClicked = { /*TODO*/ }, onCloseCameraClicked = { /*TODO*/ }) {

    }
}