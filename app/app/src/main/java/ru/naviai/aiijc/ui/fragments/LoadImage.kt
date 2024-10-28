package ru.naviai.aiijc.ui.fragments

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.FiltersParams
import ru.naviai.aiijc.ImageRect
import ru.naviai.aiijc.adjustBitmap
import ru.naviai.aiijc.ui.EditRectangle
import ru.naviai.aiijc.ui.SelectField
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun LoadImage(
    initialBitmap: Bitmap,
    onReady: (Bitmap, ImageRect, String) -> Unit,
    onBack: () -> Unit,
    onFilters: () -> Unit,
    startType: String,
    filters: FiltersParams
) {
    val image by remember {
        mutableStateOf(
            adjustBitmap(initialBitmap, filters.brightness, filters.saturation, filters.sharpness)
        )
    }
    var type by remember { mutableStateOf(startType) }
    var size = Offset.Zero

    with(LocalDensity.current) {
        val screenHeight = LocalConfiguration.current.screenHeightDp
        val screenWidth = LocalConfiguration.current.screenWidthDp

        val imageWidth = min(screenWidth.dp.toPx(), image.width.toFloat())
        val imageHeight = image.height / image.width.toFloat() * imageWidth

        val zoneHeight = min(
            (screenHeight * (3f / 8) * 2 - 16 * 2).dp.toPx(),
            imageHeight
        )

        val base = screenHeight.dp.toPx() * (3f / 8) - zoneHeight / 2
        val maxOffsetY = base + zoneHeight / 2f - 45.dp.toPx()
        val minOffsetY = base - (imageHeight - zoneHeight / 2f) + 45.dp.toPx()

        val maxOffsetX = imageWidth / 2f - 45.dp.toPx()

        var offset by remember {
            mutableStateOf(Offset(0f, base))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.TopCenter,
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

            Canvas(
                modifier = Modifier
                    .width(image.width.toDp())
                    .height(image.height.toDp())
            ) {
                val o1 = Offset(
                    (screenWidth.dp.toPx() - size.x) / 2f,
                    (screenHeight.dp.toPx() * 3 / 8 - size.y / 2)
                )
                val o2 = Offset(
                    size.x,
                    size.y
                )

                drawRect(
                    color = Color.Black.copy(alpha = 0.5f), // Transparent black
                    size = Size(
                        o1.x,
                        size.y
                    ),
                    topLeft = Offset(
                        0f,
                        o1.y
                    )
                )

                drawRect(
                    color = Color.Black.copy(alpha = 0.5f), // Transparent black
                    size = Size(
                        this.size.width - o1.x - o2.x,
                        size.y
                    ),
                    topLeft = Offset(
                        o1.x + o2.x,
                        o1.y
                    )
                )


                drawRect(
                    color = Color.Black.copy(alpha = 0.5f), // Transparent black
                    size = Size(
                        this.size.width,
                        o1.y
                    ),
                    topLeft = Offset.Zero
                )


                drawRect(
                    color = Color.Black.copy(alpha = 0.5f), // Transparent black
                    size = Size(
                        this.size.width,
                        this.size.height - o1.y - o2.y
                    ),
                    topLeft = Offset(
                        0f,
                        o1.y + o2.y
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Filled.ExitToApp,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.offset(y = (-screenHeight * (1f / 8)).dp),
                contentAlignment = Alignment.Center
            ) {
                size = EditRectangle(
                    minHeight = 90.dp.toPx(),
                    maxHeight = min(
                        min(
                            zoneHeight,
                            zoneHeight - 2 * (offset.y - base)
                        ),
                        2 * offset.y + 2 * imageHeight - 2 * screenHeight.dp.toPx() * (3f / 8)
                    ),
                    minWidth = 90.dp.toPx(),
                    maxWidth = abs(imageWidth - abs(2 * offset.x))
                )
            }

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Bottom
            ) {
                Column(
                    modifier = Modifier.height((screenHeight / 3).dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SelectField(
                        modifier = Modifier.width(200.dp),
                        label = stringResource(R.string.label_type),
                        options = listOf(
                            stringResource(R.string.type_circle),
                            stringResource(R.string.type_rectangle),
                            stringResource(R.string.type_quad),
                        ),
                        onChange = {
                            type = it
                        },
                        value = type
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.width((screenWidth / 2).dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton(onClick = {
                                val buffer = IntOffset(
                                    ((imageWidth - size.x) / 2 - offset.x).roundToInt(),
                                    (3f * screenHeight.dp.toPx() / 8 - size.y / 2f - offset.y).roundToInt()
                                )

                                onReady(
                                    initialBitmap,
                                    ImageRect(
                                        IntOffset(offset.x.roundToInt(), offset.y.roundToInt()),
                                        IntOffset(size.x.roundToInt(), size.y.roundToInt()),
                                        IntOffset(
                                            (buffer.x / imageWidth * image.width).roundToInt(),
                                            (buffer.y / imageHeight * image.height).roundToInt(),
                                        ),
                                        IntOffset(
                                            (size.x.roundToInt() / imageWidth * image.width).roundToInt(),
                                            (size.y.roundToInt() / imageHeight * image.height).roundToInt()
                                        ),
                                        Offset(
                                            ((screenWidth.dp.toPx() - size.x) / 2),
                                            (screenHeight.dp.toPx() * 3 / 8 - size.y / 2)
                                        ),
                                        Offset(
                                            size.x,
                                            size.y
                                        ),
                                        IntOffset(
                                            image.width.toDp().value.roundToInt(),
                                            image.height.toDp().value.roundToInt(),
                                        )
                                    ),
                                    type
                                )
                            }) {
                                Image(
                                    painter = painterResource(R.drawable.ellipse),
                                    contentDescription = null,
                                )
                            }
                        }

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Row(
                                modifier = Modifier.width((screenWidth / 2).dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                IconButton(onClick = onFilters) {
                                    Image(
                                        painter = painterResource(R.drawable.tune),
                                        contentDescription = null,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

