package ru.naviai.aiijc.ui.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.FiltersParams
import ru.naviai.aiijc.ImageRect
import ru.naviai.aiijc.Model
import ru.naviai.aiijc.toSerializable
import ru.naviai.aiijc.ui.fragments.Filters
import ru.naviai.aiijc.ui.fragments.LoadImage
import ru.naviai.aiijc.ui.fragments.Photo
import ru.naviai.aiijc.ui.fragments.Results

enum class ScreenState {
    Camera, LoadImage, Results, Filters, History
}

@Composable
fun MainScreen(
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    var opencvLoaded by remember { mutableStateOf(false) }

    if (!opencvLoaded) {
        if (OpenCVLoader.initLocal()) {
            Log.i("kilo", "OpenCV loaded")
            opencvLoaded = true
        } else {
            Log.i("kilo", "OpenCV not loaded")
        }
    }

    val resources = LocalContext.current.resources

    var state by remember { mutableStateOf(ScreenState.Camera) }
    var previousState by remember { mutableStateOf(ScreenState.Camera) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var initialBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var filters by remember { mutableStateOf(FiltersParams()) }
    var needSkipSave by remember { mutableStateOf(false) }

    var imageRect by remember {
        mutableStateOf(
            ImageRect(
                IntOffset.Zero.toSerializable(), IntOffset.Zero.toSerializable()
            )
        )
    }
    var model by remember { mutableStateOf<Model?>(null) }

    var type by remember {
        mutableStateOf(resources.getString(R.string.type_circle))
    }

    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (state) {
            ScreenState.Camera -> {
                Photo(
                    onMenu = {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    },
                    onLoad = { bitmap, newType ->
                        previousState = ScreenState.Camera
                        state = ScreenState.LoadImage
                        currentBitmap = bitmap
                        initialBitmap = bitmap
                        type = newType
                    },
                    onCapture = { bitmap, rect, newType ->
                        previousState = ScreenState.Camera
                        state = ScreenState.Results
                        currentBitmap = bitmap
                        initialBitmap = bitmap
                        imageRect = rect
                        type = newType
                    },
                    startType = type,
                    onHistory = {
                        state = ScreenState.History
                        type = it
                    }
                )
            }

            ScreenState.LoadImage -> {
                LoadImage(
                    currentBitmap!!,
                    onReady = { bitmap, rect, newType ->
                        previousState = ScreenState.LoadImage
                        state = ScreenState.Results
                        currentBitmap = bitmap
                        initialBitmap = bitmap
                        imageRect = rect
                        type = newType
                    },
                    onBack = {
                        state = ScreenState.Camera
                        type = it
                    },
                    startType = type,
                    filters = filters,
                    onFilters = {
                        previousState = state
                        state = ScreenState.Filters
                    }
                )
            }

            ScreenState.Results -> {
                currentBitmap?.let {
                    Results(
                        it,
                        imageRect = imageRect,
                        initialType = type,
                        onBack = {
                            needSkipSave = false
                            previousState = state
                            state = ScreenState.Camera
                        },
                        lastModel = model,
                        onModelLoaded = { newModel -> model = newModel },
                        onFilters = {
                            needSkipSave = false
                            previousState = state
                            state = ScreenState.Filters
                        },
                        filters = filters,
                        needToSkipSaveFirst = needSkipSave
                    )
                }
            }

            ScreenState.Filters -> {
                initialBitmap?.let {
                    Filters(
                        bitmap = it,
                        imageRect = imageRect,
                        onReady = { newFilters ->
                            state = previousState
                            previousState = ScreenState.Filters
                            filters = newFilters
                        },
                        filters
                    )
                }
            }

            ScreenState.History -> {
                History(
                    onLoad = { initialBitmapLoad, filtersParamsLoad, imageRectLoad, typeLoad ->
                        needSkipSave = true
                        previousState = state
                        state = ScreenState.Results
                        initialBitmap = initialBitmapLoad
                        currentBitmap = initialBitmap
                        filters = filtersParamsLoad
                        imageRect = imageRectLoad
                        type = when (typeLoad) {
                            Model.PredictionsType.ALL -> context.resources.getString(
                                R.string.type_all
                            )
                            Model.PredictionsType.CIRCLE -> context.resources.getString(
                                R.string.type_circle
                            )
                            Model.PredictionsType.RECTANGLE -> context.resources.getString(
                                R.string.type_rectangle
                            )
                        }
                    },
                    onBack = {
                        state = ScreenState.Camera
                        previousState = ScreenState.History
                    }
                )
            }
        }
    }
}

