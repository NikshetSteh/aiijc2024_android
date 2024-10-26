package ru.naviai.aiijc.ui.fragments

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.ImageRect
import ru.naviai.aiijc.Model
import ru.naviai.aiijc.ModelResults
import ru.naviai.aiijc.makePrediction
import ru.naviai.aiijc.ui.EditRectangle
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt


@Composable
fun Results(
    bitmap: Bitmap,
    imageRect: ImageRect,
    type: String,
    onBack: () -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    var prediction by remember {
        mutableStateOf<ModelResults?>(null)
    }
    val context = LocalContext.current
    val model by remember { mutableStateOf(Model(context)) }

    var needPrediction by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }

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
            Log.i("kilo", "Content offsets: ${imageRect.contentOffset.x} ${imageRect.contentOffset.y}")
            cropped = Bitmap.createBitmap(
                bitmap,
                imageRect.contentOffset.x,
                imageRect.contentOffset.y,
                imageRect.imageSize.x,
                imageRect.imageSize.y
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
        modifier = Modifier.fillMaxSize().background(Color.Black),
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
                    .width(imageRect.imageSize.y.toDp()),
                contentAlignment = Alignment.Center
            ) {
                if (prediction != null) {
                    val restoredImage = Bitmap.createScaledBitmap(
                        prediction!!.bitmap,
                        imageRect.imageSize.x,
                        imageRect.imageSize.y,
                        true
                    )
                    Image(
                        bitmap = restoredImage.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.None
                    )
                }
                EditRectangle(
                    imageRect.imageSize.y.toFloat(),
                    imageRect.imageSize.y.toFloat(),
                    imageRect.imageSize.x.toFloat(),
                    imageRect.imageSize.x.toFloat(),
                )
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
                            Text(prediction?.count.toString())
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
