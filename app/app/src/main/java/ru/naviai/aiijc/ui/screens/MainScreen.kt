package ru.naviai.aiijc.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.transition.Slide
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.image.cropview.ImageCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.Model
import ru.naviai.aiijc.ModelResults
import ru.naviai.aiijc.brightness
import ru.naviai.aiijc.contrast
import ru.naviai.aiijc.scaleBitmapWithBlackMargins
import ru.naviai.aiijc.sharpness
import ru.naviai.aiijc.ui.SelectField
import ru.naviai.aiijc.ui.fragments.Crop
import ru.naviai.aiijc.ui.fragments.CropBottom
import ru.naviai.aiijc.ui.fragments.FiltersBottom
import ru.naviai.aiijc.ui.fragments.FiltersParams
import ru.naviai.aiijc.ui.fragments.PhotoBottom
import ru.naviai.aiijc.ui.fragments.PhotoTop
import ru.naviai.aiijc.ui.fragments.Results
import ru.naviai.aiijc.ui.fragments.ResultsBottom

enum class ScreenState {
    Camera, Crop, Result, Filters
}

@Composable
fun MainScreen(
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    val resources = LocalContext.current.resources

    var state by remember { mutableStateOf(ScreenState.Camera) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageCapture by remember {
        mutableStateOf<ImageCapture?>(
            null
        )
    }

    var type by remember {
        mutableStateOf(resources.getString(R.string.type_circle))
    }

    var imageCrop: ImageCrop? = null

    val model = Model(LocalContext.current)

    var isLoading by remember { mutableStateOf(false) }
    var needPrediction by remember { mutableStateOf(false) }
    var flashlight by remember { mutableStateOf(false) }

    var prediction by remember { mutableStateOf<ModelResults?>(null) }

//    val filtersParams by rememberUpdatedState(
//        FiltersParams(
//            contrast = 0f,
//            brightness = 0f,
//            sharpness = 0f
//        )
//    )

    if (needPrediction) {
        needPrediction = false
        makePrediction(
            model,
            currentBitmap!!,
            onResult = {
                isLoading = false
                prediction = it
            },
            when (type) {
                resources.getString(R.string.type_circle) -> {
                    Model.PredictionsType.CIRCLE
                }

                resources.getString(R.string.type_rectangle) -> {
                    Model.PredictionsType.RECTANGLE
                }

                resources.getString(R.string.type_quad) -> {
                    Model.PredictionsType.QUAD
                }

                else -> {
                    Model.PredictionsType.CIRCLE
                }
            }
        )
    }

    IconButton(onClick = {
        scope.launch {
            drawerState.apply {
                if (isClosed) open() else close()
            }
        }
    }) {
        Icon(Icons.Outlined.Menu, contentDescription = "Menu")
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
                    imageCapture = PhotoTop(flashlight)
                }

                ScreenState.Crop -> {
                    imageCrop = Crop(currentBitmap)
                }

                ScreenState.Filters -> {
//                    Filters(
//                        applyFilters(
//                            currentBitmap!!,
//                            filtersParams
//                        )
//                    )
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
                        flashlight,
                        onCapture = {
                            currentBitmap = it
                            state = ScreenState.Crop
                        },
                        onFlashLight = {
                            flashlight = !flashlight
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
                        },
                        onEdit = {
                            state = ScreenState.Filters
                        }
                    )
                }

                ScreenState.Filters -> {
//                    FiltersBottom(
//                        filtersParams,
//                        onChange = {
//                            filtersParams.brightness = it.brightness
//                            filtersParams.contrast = it.contrast
//                            filtersParams.sharpness = it.sharpness
//                        }
//                    )

//                    var substate by remember {
//                        mutableStateOf(0f)
//                    }

//                    Slider(
//                        modifier=Modifier.width(250.dp),
//                        value=substate,
//                        onValueChange = {
//                            substate = it
//                        }
//                    )
                }

                else -> {
                    ResultsBottom(
                        onReload = {
                            isLoading = true
                            needPrediction = true
                            prediction = null
                        },
                        onNewImage = {
                            prediction = null
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
                options = listOf(
                    resources.getString(R.string.type_circle),
                    resources.getString(R.string.type_rectangle),
                    resources.getString(R.string.type_quad),
                ),
                label = resources.getString(R.string.label_type),
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

fun makePrediction(
    model: Model,
    bitmap: Bitmap,
    onResult: (ModelResults) -> Unit,
    type: Model.PredictionsType
) {
    CoroutineScope(Dispatchers.Main).launch {
        val result = withContext(Dispatchers.IO) {
            model.predict(
                bitmap,
                when (type) {
                    Model.PredictionsType.CIRCLE -> {
                        listOf(0)
                    }

                    Model.PredictionsType.QUAD -> {
                        listOf(1)
                    }

                    Model.PredictionsType.RECTANGLE -> {
                        listOf(2)
                    }
                }
            )
        }
        onResult(result)
    }
}


fun applyFilters(
    bitmap: Bitmap,
    filtersParams: FiltersParams
): Bitmap {
    var buffer = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(buffer)
    canvas.drawBitmap(bitmap, 0f, 0f, null)

    buffer = contrast(buffer, filtersParams.contrast.toDouble())
    buffer = brightness(buffer, filtersParams.brightness.toDouble())
    buffer = sharpness(buffer, filtersParams.sharpness.toDouble())

    return buffer
}