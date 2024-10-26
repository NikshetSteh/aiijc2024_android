package ru.naviai.aiijc.ui.fragments

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.image.cropview.CropType
import com.image.cropview.EdgeType
import com.image.cropview.ImageCrop
import ru.NaviAI.aiijc.R
import kotlin.math.roundToInt

@Composable
fun Crop(
    bitmap: Bitmap?
): ImageCrop? {
    val imageCrop = bitmap?.let { ImageCrop(it) }


    Box(
        contentAlignment = androidx.compose.ui.Alignment.Center,
        modifier = Modifier
            .width(320.dp)
            .height(320.dp)
    ) {
        imageCrop?.ImageCropView(
            modifier = Modifier
                .width(320.dp)
                .height(320.dp),
            guideLineColor = Color.LightGray,
            guideLineWidth = 2.dp,
            edgeCircleSize = 5.dp,
            showGuideLines = true,
            cropType = CropType.FREE_STYLE,
            edgeType = EdgeType.CIRCULAR,
        )

//        Rectangle(30f, 320f)
    }

    return imageCrop
}


//@Composable
//fun Rectangle() {
//    with(LocalDensity.current) {
//        var size by remember {
//            mutableStateOf(
//                Offset(
//                    320.dp.toPx(), 320.dp.toPx()
//                )
//            )
//        }
//
//        val min = 120.dp.toPx()
//
//        MoveIcon(
//            offsetX = size.x / 2,
//            offsetY = size.y / 2,
//            callback = { deltaX, deltaY ->
//                run {
//                    var bufferX = size.x + deltaX * 2
//                    var bufferY = size.y + deltaY * 2
//
//                    if (bufferX > 320.dp.toPx()) {
//                        bufferX = 320.dp.toPx()
//                    } else if (bufferX < min) {
//                        bufferX = min
//                    }
//
//                    if (bufferY > 320.dp.toPx()) {
//                        bufferY = 320.dp.toPx()
//                    } else if (bufferY < min) {
//                        bufferY = min
//                    }
//
//                    size = Offset(bufferX, bufferY)
//                }
//            }
//        )
//
//        MoveIcon(
//            offsetX = -size.x / 2,
//            offsetY = -size.y / 2,
//            callback = { deltaX, deltaY ->
//                run {
//                    var bufferX = size.x - deltaX * 2
//                    var bufferY = size.y - deltaY * 2
//
//                    if (bufferX > 320.dp.toPx()) {
//                        bufferX = 320.dp.toPx()
//                    } else if (bufferX < min) {
//                        bufferX = min
//                    }
//
//                    if (bufferY > 320.dp.toPx()) {
//                        bufferY = 320.dp.toPx()
//                    } else if (bufferY < min) {
//                        bufferY = min
//                    }
//
//                    size = Offset(bufferX, bufferY)
//                }
//            }
//        )
//
//        MoveIcon(
//            offsetX = -size.x / 2,
//            offsetY = size.y / 2,
//            callback = { deltaX, deltaY ->
//                run {
//                    var bufferX = size.x - deltaX * 2
//                    var bufferY = size.y + deltaY * 2
//
//                    if (bufferX > 320.dp.toPx()) {
//                        bufferX = 320.dp.toPx()
//                    } else if (bufferX < min) {
//                        bufferX = min
//                    }
//
//                    if (bufferY > 320.dp.toPx()) {
//                        bufferY = 320.dp.toPx()
//                    } else if (bufferY < min) {
//                        bufferY = min
//                    }
//
//                    size = Offset(bufferX, bufferY)
//                }
//            }
//        )
//
//        MoveIcon(
//            offsetX = size.x / 2,
//            offsetY = -size.y / 2,
//            callback = { deltaX, deltaY ->
//                run {
//                    var bufferX = size.x + deltaX * 2
//                    var bufferY = size.y - deltaY * 2
//
//                    if (bufferX > 320.dp.toPx()) {
//                        bufferX = 320.dp.toPx()
//                    } else if (bufferX < min) {
//                        bufferX = min
//                    }
//
//                    if (bufferY > 320.dp.toPx()) {
//                        bufferY = 320.dp.toPx()
//                    } else if (bufferY < min) {
//                        bufferY = min
//                    }
//
//                    size = Offset(bufferX, bufferY)
//                }
//            }
//        )
//
//    }
//}
//
//@Composable
//fun MoveIcon(
//    offsetX: Float,
//    offsetY: Float,
//    callback: (Float, Float) -> Unit
//) {
//    Icon(
//        Icons.Filled.AddCircle,
//        contentDescription = null,
//        modifier = Modifier
//            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
//            .pointerInput(Unit) {
//                detectDragGestures { change, dragAmount ->
//                    change.consume()
//                    callback(dragAmount.x, dragAmount.y)
//                }
//            }
//    )
//}

@Composable
fun CropBottom(
    imageCrop: ImageCrop?,
    onCrop: (Bitmap) -> Unit,
    onEdit: () -> Unit
) {
    Button(
        onClick = onEdit
    ) {
        Text(LocalContext.current.resources.getString(R.string.action_filters))
    }

    Spacer(modifier = Modifier.width(16.dp))

    Button(
        onClick = {
            imageCrop?.onCrop()?.let { onCrop(it) }
        }
    ) {
        Text(LocalContext.current.resources.getString(R.string.action_continue))
    }
}