package com.kroncoders.android.ui.components

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import org.webrtc.VideoTrack

@Composable
fun FloatingVideoRenderer(
    modifier: Modifier = Modifier,
    videoTrack: VideoTrack,
    parentBounds: IntSize,
    paddingValues: PaddingValues
) {

    var videoSize by remember { mutableStateOf(IntSize(0, 0)) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val offset by animateOffsetAsState(targetValue = Offset(offsetX, offsetY))
    val density = LocalDensity.current

    LaunchedEffect(parentBounds.width) {
        offsetX = 0f
        offsetY = 0f
    }

    val paddingOffset = density.run { 16.dp.toPx() }

    Card(
        elevation = 8.dp,
        modifier = Modifier
            .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
            .pointerInput(parentBounds) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val newOffsetX = (offsetX + dragAmount.x)
                        .coerceAtLeast(
                            -calculateHorizontalOffsetBounds(
                                parentBounds = parentBounds,
                                paddingValues = paddingValues,
                                floatingVideoSize = videoSize,
                                density = density,
                                offset = paddingOffset * 2
                            )
                        )
                        .coerceAtMost(0f)
                    val newOffsetY = (offsetY + dragAmount.y)
                        .coerceAtLeast(0f)
                        .coerceAtMost(
                            calculateVerticalOffsetBounds(
                                parentBounds = parentBounds,
                                paddingValues = paddingValues,
                                floatingVideoSize = videoSize,
                                density = density,
                                offset = paddingOffset * 2
                            )
                        )
                    offsetX = newOffsetX
                    offsetY = newOffsetY
                }
            }
            .then(modifier)
            .padding(16.dp)
            .onGloballyPositioned { videoSize = it.size },
        shape = RoundedCornerShape(16.dp)
    ) {
        VideoRenderer(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
            videoTrack = videoTrack
        )
    }

}

private fun calculateHorizontalOffsetBounds(
    parentBounds: IntSize,
    paddingValues: PaddingValues,
    floatingVideoSize: IntSize,
    density: Density,
    offset: Float
): Float {
    val rightPadding = density.run { paddingValues.calculateRightPadding(LayoutDirection.Ltr).toPx() }
    return parentBounds.width - rightPadding - floatingVideoSize.width - offset
}

private fun calculateVerticalOffsetBounds(
    parentBounds: IntSize,
    paddingValues: PaddingValues,
    floatingVideoSize: IntSize,
    density: Density,
    offset: Float
): Float {
    val bottomPadding = density.run { paddingValues.calculateBottomPadding().toPx() }
    return parentBounds.height - bottomPadding - floatingVideoSize.height - offset
}