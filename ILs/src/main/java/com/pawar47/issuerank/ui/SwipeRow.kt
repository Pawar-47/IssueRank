package com.pawar47.issuerank.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * iOS-style row actions: swipe right for the row's primary action (labelled by the caller),
 * swipe left for Delete. A short swipe parks the button open, a long one commits it.
 */
@Composable
fun SwipeRow(
    actionLabel: String,
    actionTint: Color,
    onAction: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    content: @Composable () -> Unit
) {
    val colors = Ios.colors
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val revealPx = with(density) { 92.dp.toPx() }
    val commitPx = revealPx * 0.85f

    Box(modifier.fillMaxWidth().clip(shape)) {
        Row(Modifier.matchParentSize()) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(92.dp)
                    .background(actionTint)
                    .iosClick {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onAction()
                        scope.launch { offsetX.animateTo(0f) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CheckGlyph(Color.White)
                    IText(
                        actionLabel,
                        style = IosType.caption,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(92.dp)
                    .background(colors.red)
                    .iosClick {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDelete()
                        scope.launch { offsetX.animateTo(0f) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TrashGlyph(Color.White)
                    IText("Delete", style = IosType.caption, color = Color.White, maxLines = 1)
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .background(colors.card)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(revealPx) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val current = offsetX.value
                            scope.launch {
                                when {
                                    current <= -commitPx -> {
                                        onDelete()
                                        offsetX.snapTo(0f)
                                    }

                                    current >= commitPx -> {
                                        onAction()
                                        offsetX.snapTo(0f)
                                    }

                                    current <= -revealPx * 0.3f -> offsetX.animateTo(-revealPx)
                                    current >= revealPx * 0.3f -> offsetX.animateTo(revealPx)
                                    else -> offsetX.animateTo(0f)
                                }
                            }
                        },
                        onDragCancel = { scope.launch { offsetX.animateTo(0f) } },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                offsetX.snapTo(
                                    (offsetX.value + dragAmount).coerceIn(-revealPx, revealPx)
                                )
                            }
                        }
                    )
                }
                .padding(0.dp)
        ) {
            content()
        }
    }
}
