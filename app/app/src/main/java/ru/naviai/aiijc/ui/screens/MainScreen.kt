package ru.naviai.aiijc.ui.screens

import android.graphics.Bitmap
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.image.cropview.ImageCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.naviai.aiijc.Model
import ru.naviai.aiijc.ModelResults
import ru.naviai.aiijc.scaleBitmapWithBlackMargins
import ru.naviai.aiijc.ui.SelectField
import ru.naviai.aiijc.ui.fragments.Crop
import ru.naviai.aiijc.ui.fragments.CropBottom
import ru.naviai.aiijc.ui.fragments.PhotoBottom
import ru.naviai.aiijc.ui.fragments.PhotoTop
import ru.naviai.aiijc.ui.fragments.Results
import ru.naviai.aiijc.ui.fragments.ResultsBottom

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

    var prediction by remember { mutableStateOf<ModelResults?>(null) }

    if (needPrediction) {
        needPrediction = false
        makePrediction(
            model,
            currentBitmap!!,
            onResult = {
                isLoading = false
                prediction = it
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
            when (state) {
                ScreenState.Camera -> {
                    imageCapture = PhotoTop()
                }

                ScreenState.Crop -> {
                    imageCrop = Crop(currentBitmap)
                }

                else -> {
                    Results(
                        prediction?.bitmap
                    )
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
                    ResultsBottom(
                        onReload = {
                            isLoading = true
                            needPrediction = true
                            prediction = null
                        },
                        onNewImage = {
                            state = ScreenState.Camera
                        },
                        isLoading = isLoading,
                        count = if (prediction != null) prediction?.count else null
                    )
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