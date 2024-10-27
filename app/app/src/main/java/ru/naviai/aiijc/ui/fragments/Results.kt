package ru.naviai.aiijc.ui.fragments

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.ImageRect
import ru.naviai.aiijc.Model
import ru.naviai.aiijc.ModelResults
import ru.naviai.aiijc.makePrediction
import ru.naviai.aiijc.ui.EditRectangle
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt


enum class Mode {
    NONE, ADD, DELETE
}


@Composable
fun Results(
    bitmap: Bitmap,
    imageRect: ImageRect,
    type: String,
    onBack: () -> Unit,
    onModelLoaded: (Model) -> Unit,
    lastModel: Model?
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val screenWidth = LocalConfiguration.current.screenWidthDp
    var prediction by remember {
        mutableStateOf<ModelResults?>(null)
    }
    val context = LocalContext.current
    val model by remember { mutableStateOf(lastModel ?: Model(context)) }
    var modelLoaded by remember { mutableStateOf(false) }
    if (!modelLoaded) {
        modelLoaded = true
        onModelLoaded(model)
    }

    var needPrediction by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }

    var mode by remember {
        mutableStateOf(Mode.NONE)
    }

    if (needPrediction) {
        Log.i("kilo", "Bitmap sizes: ${bitmap.width} ${bitmap.height}")
        Log.i("kilo", "Rect sizes: ${imageRect.imageSize.x} ${imageRect.imageSize.y}")
        Log.i("kilo", "Rect offsets: ${imageRect.imageOffset.x} ${imageRect.imageOffset.y}")

        val cropped: Bitmap
        if (imageRect.contentOffset == null) {
            cropped = Bitmap.createBitmap(
                bitmap,
                (bitmap.width - imageRect.imageSize.x) / 2,
                (bitmap.height.toFloat() * 3 / 8 - imageRect.imageSize.y / 2).roundToInt(),
                imageRect.imageSize.x,
                imageRect.imageSize.y
            )
        } else {
            Log.i(
                "kilo",
                "Content offsets: ${imageRect.contentOffset.x} ${imageRect.contentOffset.y}"
            )
            cropped = Bitmap.createBitmap(
                bitmap,
                imageRect.contentOffset.x,
                imageRect.contentOffset.y,
                imageRect.contentSize?.x ?: imageRect.imageSize.x,
                imageRect.contentSize?.y ?: imageRect.imageSize.y
            )
        }

        val buffer = Bitmap.createScaledBitmap(cropped, 640, 640, true)
        val file = File(context.cacheDir, "image.jpg")
        val outputStream = FileOutputStream(file)
        buffer.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        needPrediction = false
        makePrediction(
            model,
            buffer,
            onResult = {
                isLoading = false
                prediction = it
            },
            when (type) {
                stringResource(R.string.type_circle) -> {
                    Model.PredictionsType.CIRCLE
                }

                stringResource(R.string.type_rectangle) -> {
                    Model.PredictionsType.RECTANGLE
                }

                stringResource(R.string.type_quad) -> {
                    Model.PredictionsType.QUAD
                }

                else -> {
                    Model.PredictionsType.CIRCLE
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.TopCenter
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .offset {
                    imageRect.imageOffset
                },
            contentScale = ContentScale.FillWidth
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        with(LocalDensity.current) {
            Box(
                modifier = Modifier
                    .offset(y = (-screenHeight / 8).dp)
                    .height(imageRect.imageSize.x.toDp())
                    .width(screenWidth.dp),
                contentAlignment = Alignment.Center
            ) {
                EditRectangle(
                    imageRect.imageSize.y.toFloat(),
                    imageRect.imageSize.y.toFloat(),
                    imageRect.imageSize.x.toFloat(),
                    imageRect.imageSize.x.toFloat(),
                )
                if (prediction != null) {
                    ResultsItems(
                        items = prediction!!.items,
                        imageRect.imageSize,
                        prediction!!.meanSize.toDp().value,
                        actionMode = mode,
                        onChange = { items ->
                            prediction = ModelResults(
                                items.size,
                                items,
                                prediction!!.meanSize
                            )
                        }
                    )
                }

                if (mode == Mode.ADD) {
                    Box(
                        modifier = Modifier
                            .width(imageRect.imageSize.x.toDp())
                            .height(imageRect.imageSize.y.toDp())
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    Log.i("kilo", "Offset: ${offset.x} ${offset.y}")
                                    prediction.let {
                                        prediction = ModelResults(
                                            it!!.items.size + 1,
                                            it.items +
                                                    Item(
                                                        IntOffset(
                                                            (offset.x / imageRect.imageSize.x * 640).roundToInt(),
                                                            (offset.y / imageRect.imageSize.y * 640).roundToInt(),
                                                        )
                                                    ),
                                            it.meanSize
                                        )
                                    }
                                }
                            }
                    )
                }
            }
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
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(64.dp)
                            .width(64.dp)
                    )
                } else {
                    Row {
                        IconButton(onClick = {
                            mode = if (mode != Mode.DELETE) {
                                Mode.DELETE
                            } else {
                                Mode.NONE
                            }
                        }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null,
                                tint = if (mode == Mode.DELETE) Color.White else Color.Gray
                            )
                        }
                        Card(
                            modifier = Modifier
                                .height(40.dp)
                                .width(40.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White,
                            ),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    prediction?.count.toString(),
                                    color = Color.Black
                                )
                            }
                        }

                        IconButton(onClick = {
                            mode = if (mode != Mode.ADD) {
                                Mode.ADD
                            } else {
                                Mode.NONE
                            }
                        }) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = null,
                                tint = if (mode == Mode.ADD) Color.White else Color.Gray
                            )
                        }
                    }
                }

                Button(onClick = onBack) {
                    Text(stringResource(R.string.action_back))
                }
            }
        }
    }
}


class Item(
    val offset: IntOffset
)


@Composable
fun ResultItem(
    title: String,
    fontSize: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painterResource(R.drawable.ellipse_item),
            contentDescription = null,
            modifier = Modifier.clickable(
                onClick = onClick
            )
        )
        Text(
            text = title,
            fontSize = (0.5 * fontSize).sp
        )
    }
}


@Composable
fun ResultsItems(
    items: List<Item>,
    imageSize: IntOffset,
    size: Float,
    onChange: (List<Item>) -> Unit,
    actionMode: Mode = Mode.NONE
) {
    var counter = 1

    for (item in items) {
//        Log.i("kilo", "Item: ${item.value} offset: ${item.offset.x} ${item.offset.y}")
        ResultItem(
            (counter++).toString(),
            modifier = Modifier
                .offset {
                    IntOffset(
                        (-imageSize.x / 2 + item.offset.x.toFloat() / 640 * imageSize.x).roundToInt(),
                        (-imageSize.y / 2 + item.offset.y.toFloat() / 640 * imageSize.y).roundToInt()
                    )
                }
                .size(size.dp),
            fontSize = size,
            onClick = {
                if (actionMode == Mode.DELETE) {
                    onChange(items - item)
                }
            }
        )
    }
}