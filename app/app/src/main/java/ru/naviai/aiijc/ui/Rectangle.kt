package ru.naviai.aiijc.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import ru.NaviAI.aiijc.R
import kotlin.math.roundToInt

@Composable
fun EditRectangle(
    minHeight: Float,
    maxHeight: Float,
    minWidth: Float,
    maxWidth: Float
): Offset {
    with(LocalDensity.current) {
        var size by remember {
            mutableStateOf(
                Offset(
                    maxWidth, maxHeight
                )
            )
        }

        if (size.x > maxWidth) {
            size = Offset(maxWidth, size.y)
        }
        if(size.y > maxHeight) {
            size = Offset(size.x, maxHeight)
        }

        Log.i("kilo", "Size: {${size.x}, ${size.y}}")

        val iconOffset = -(painterResource(R.drawable.corner_1).intrinsicSize.height) / 2

        MoveIcon(
            offsetX = size.x / 2 + iconOffset,
            offsetY = size.y / 2 + iconOffset,
            bitmap = painterResource(id = R.drawable.corner_1),
            callback = { deltaX, deltaY ->
                run {
                    var bufferX = size.x + deltaX * 2
                    var bufferY = size.y + deltaY * 2

                    if (bufferX > maxWidth) {
                        bufferX = maxWidth
                    } else if (bufferX < minWidth) {
                        bufferX = minWidth
                    }

                    if (bufferY > maxHeight) {
                        bufferY = maxHeight
                    } else if (bufferY < minHeight) {
                        bufferY = minHeight
                    }

                    size = Offset(bufferX, bufferY)
                }
            }
        )

        MoveIcon(
            offsetX = -size.x / 2 - iconOffset,
            offsetY = -size.y / 2 - iconOffset,
            bitmap = painterResource(id = R.drawable.corner_2),
            callback = { deltaX, deltaY ->
                run {
                    var bufferX = size.x - deltaX * 2
                    var bufferY = size.y - deltaY * 2

                    if (bufferX > maxWidth) {
                        bufferX = maxWidth
                    } else if (bufferX < minWidth) {
                        bufferX = minWidth
                    }

                    if (bufferY > maxHeight) {
                        bufferY = maxHeight
                    } else if (bufferY < minHeight) {
                        bufferY = minHeight
                    }

                    size = Offset(bufferX, bufferY)
                }
            }
        )

        MoveIcon(
            offsetX = -size.x / 2 - iconOffset,
            offsetY = size.y / 2 + iconOffset,
            bitmap = painterResource(id = R.drawable.corner_4),
            callback = { deltaX, deltaY ->
                run {
                    var bufferX = size.x - deltaX * 2
                    var bufferY = size.y + deltaY * 2

                    if (bufferX > maxWidth) {
                        bufferX = maxWidth
                    } else if (bufferX < minWidth) {
                        bufferX = minWidth
                    }

                    if (bufferY > maxHeight) {
                        bufferY = maxHeight
                    } else if (bufferY < minHeight) {
                        bufferY = minHeight
                    }

                    size = Offset(bufferX, bufferY)
                }
            }
        )

        MoveIcon(
            offsetX = size.x / 2 + iconOffset,
            offsetY = -size.y / 2 - iconOffset,
            bitmap = painterResource(id = R.drawable.corner_3),
            callback = { deltaX, deltaY ->
                run {
                    var bufferX = size.x + deltaX * 2
                    var bufferY = size.y - deltaY * 2

                    if (bufferX > maxWidth) {
                        bufferX = maxWidth
                    } else if (bufferX < minWidth) {
                        bufferX = minWidth
                    }

                    if (bufferY > maxHeight) {
                        bufferY = maxHeight
                    } else if (bufferY < minHeight) {
                        bufferY = minHeight
                    }

                    size = Offset(bufferX, bufferY)
                }
            }
        )
        return size
    }
}

@Composable
fun MoveIcon(
    offsetX: Float,
    offsetY: Float,
    bitmap: Painter,
    callback: (Float, Float) -> Unit
) {
    Image(
        bitmap,
        contentDescription = null,
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    callback(dragAmount.x, dragAmount.y)
                }
            }
    )
}