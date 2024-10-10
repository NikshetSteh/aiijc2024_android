package ru.naviai.aiijc.ui.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.coroutines.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.image.cropview.ImageCrop
import ru.naviai.aiijc.Model
import ru.naviai.aiijc.ModelResults
import ru.naviai.aiijc.scaleBitmapWithBlackMargins
import ru.naviai.aiijc.ui.SelectField
import ru.naviai.aiijc.ui.fragments.Crop
import ru.naviai.aiijc.ui.fragments.CropBottom
import ru.naviai.aiijc.ui.fragments.PhotoBottom
import ru.naviai.aiijc.ui.fragments.PhotoTop

enum class ScreenState {
    Camera, Crop, Result
}

@Composable
fun MainScreen() {
    var state by remember { mutableStateOf(ScreenState.Camera) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageCapture by remember {
        mutableStateOf<ImageCapture?>(
            null
        )
    }

    var type by remember {
        mutableStateOf("Circle")
    }

    var imageCrop: ImageCrop? = null

    val model = Model(LocalContext.current)

    var isLoading by remember { mutableStateOf(false) }
    var needPrediction by remember { mutableStateOf(false) }

    var resultBitmap by remember { mutableStateOf<Bitmap?>(null) }

    if (needPrediction) {
        needPrediction = false
        makePrediction(
            model,
            currentBitmap!!,
            onResult = {
                isLoading = false
                resultBitmap = it.bitmap
                Log.i("kilo", it.count.toString())
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state == ScreenState.Camera) {
                imageCapture = PhotoTop()
            } else if (state == ScreenState.Crop) {
                imageCrop = Crop(currentBitmap)
            } else {
                if (resultBitmap != null) {
                    Image(
                        bitmap = resultBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .width(320.dp)
                            .height(320.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .width(320.dp)
                            .height(320.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(64.dp),
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        )
        {
            when (state) {
                ScreenState.Camera -> {
                    PhotoBottom(
                        imageCapture = imageCapture!!,
                        onCapture = {
                            currentBitmap = it
                            state = ScreenState.Crop
                        }
                    )
                }

                ScreenState.Crop -> {
                    CropBottom(
                        imageCrop = imageCrop!!,
                        onCrop = {
                            currentBitmap = scaleBitmapWithBlackMargins(it)
                            state = ScreenState.Result
                            isLoading = true
                            needPrediction = true
                        }
                    )
                }

                else -> {
                    Button(
                        onClick = {},
                        enabled = !isLoading
                    ) {
                        Text("Restart")
                    }
                }
            }
        }


        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        )
        {
            SelectField(
                options = listOf("Circle"),
                label = "Type",
                value = type,
                onChange = {
                    if (!isLoading) {
                        type = it
                    }
                },
                disabled = isLoading
            )
        }
    }
}


fun makePrediction(model: Model, bitmap: Bitmap, onResult: (ModelResults) -> Unit) {
    CoroutineScope(Dispatchers.Main).launch {
        val result = withContext(Dispatchers.IO) {
            model.predict(bitmap)
        }
        onResult(result)
    }
}