package ru.naviai.aiijc.ui.fragments

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ru.naviai.aiijc.ui.Rectangle
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun LoadImage(
    image: Bitmap
) {
    with(LocalDensity.current) {
        val screenHeight = LocalConfiguration.current.screenHeightDp
        val screenWidth = LocalConfiguration.current.screenWidthDp

        val imageWidth = min(screenWidth.dp.toPx(), image.width.toFloat())
        val imageHeight = image.height / image.width.toFloat() * imageWidth

        val zoneHeight = min(
            (screenHeight / 3f * 2 - 16 * 2).dp.toPx(),
            imageHeight
        )

        val base = screenHeight.dp.toPx() / 3f - zoneHeight / 2
        val maxOffsetY = base + zoneHeight / 2f - 30.dp.toPx()
        val minOffsetY = base - (imageHeight - zoneHeight / 2f) + 30.dp.toPx()

        val maxOffsetX = imageWidth / 2f - 30.dp.toPx()

        var offset by remember {
            mutableStateOf(Offset(0f, base))
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.TopCenter
        ) {
            Image(
                image.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .offset {
                        IntOffset(offset.x.roundToInt(), offset.y.roundToInt())
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()

                            var bufferX = offset.x + dragAmount.x
                            var bufferY = offset.y + dragAmount.y

                            if (bufferX > maxOffsetX) {
                                bufferX = maxOffsetX
                            } else if (bufferX < -maxOffsetX) {
                                bufferX = -maxOffsetX
                            }

                            if (bufferY < minOffsetY) {
                                bufferY = minOffsetY
                            } else if (bufferY > maxOffsetY) {
                                bufferY = maxOffsetY
                            }

                            offset = Offset(bufferX, bufferY)
                        }
                    }
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {

            Box(
                modifier = Modifier.offset(y = (-screenHeight / 6f).dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Rectangle(
                    minHeight = 60.dp.toPx(),
                    maxHeight = min(
                        min(
                            zoneHeight,
                            zoneHeight - 2 * (offset.y - base)
                        ),
                        2 * offset.y + 2 * imageHeight - 2 * screenHeight.dp.toPx() / 3
                    ),
                    minWidth = 60.dp.toPx(),
                    maxWidth = abs(imageWidth - abs(2 * offset.x))
                )
            }
        }
    }
}

