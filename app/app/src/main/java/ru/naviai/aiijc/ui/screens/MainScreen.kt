package ru.naviai.aiijc.ui.screens

import android.graphics.Bitmap
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.Model
import ru.naviai.aiijc.ModelResults
import ru.naviai.aiijc.adjustBitmap
import ru.naviai.aiijc.scaleBitmapWithBlackMargins
import ru.naviai.aiijc.ui.SelectField
import ru.naviai.aiijc.ui.fragments.Crop
import ru.naviai.aiijc.ui.fragments.CropBottom
import ru.naviai.aiijc.ui.fragments.Filters
import ru.naviai.aiijc.ui.fragments.FiltersParams
import ru.naviai.aiijc.ui.fragments.LoadImage
import ru.naviai.aiijc.ui.fragments.Photo
import ru.naviai.aiijc.ui.fragments.Results
import ru.naviai.aiijc.ui.fragments.ResultsBottom

enum class ScreenState {
    Camera, LoadImage
}

@Composable
fun MainScreen(
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    val resources = LocalContext.current.resources

    var state by remember { mutableStateOf(ScreenState.Camera) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var initialBitmap by remember { mutableStateOf<Bitmap?>(null) }
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

    var lastFilterParams by remember { mutableStateOf<FiltersParams?>(null) }

    var prediction by remember { mutableStateOf<ModelResults?>(null) }

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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (state == ScreenState.Camera) {
            Photo(
                onMenu = {
                    scope.launch {
                        drawerState.apply {
                            if (isClosed) open() else close()
                        }
                    }
                },
                onLoad = {
                    state = ScreenState.LoadImage
                    currentBitmap = it
                    initialBitmap = it
                }
            )
        }else if (state == ScreenState.LoadImage) {
            LoadImage(
                currentBitmap!!
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

