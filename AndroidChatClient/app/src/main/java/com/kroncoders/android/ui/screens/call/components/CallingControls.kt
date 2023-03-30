package com.kroncoders.android.ui.screens.call.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CallingControls(
    modifier: Modifier = Modifier,
    onEndCallClicked: () -> Unit
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
                .background(Color.Red),
            onClick = onEndCallClicked
        ) {
            Icon(imageVector = Icons.Default.CallEnd, contentDescription = null, tint = Color.Black)
        }
    }
}

@Preview
@Composable
fun CallingControlsPreview() {
    CallingControls(onEndCallClicked = {})
}