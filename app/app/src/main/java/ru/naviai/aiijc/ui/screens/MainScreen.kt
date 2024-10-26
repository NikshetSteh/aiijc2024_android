package ru.naviai.aiijc.ui.screens

import android.graphics.Bitmap
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.ImageRect
import ru.naviai.aiijc.Model
import ru.naviai.aiijc.ModelResults
import ru.naviai.aiijc.ui.fragments.LoadImage
import ru.naviai.aiijc.ui.fragments.Photo
import ru.naviai.aiijc.ui.fragments.Results

enum class ScreenState {
    Camera, LoadImage, Results
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

    var imageRect by remember { mutableStateOf(ImageRect(IntOffset.Zero, IntOffset.Zero)) }

    val type by remember {
        mutableStateOf(resources.getString(R.string.type_circle))
    }

    val model = Model(LocalContext.current)

    var isLoading by remember { mutableStateOf(false) }
    var needPrediction by remember { mutableStateOf(false) }

    var prediction by remember { mutableStateOf<ModelResults?>(null) }

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
                    onLoad = {
                        state = ScreenState.LoadImage
                        currentBitmap = it
                        initialBitmap = it
                    },
                    onCapture = { bitmap, rect ->
                        state = ScreenState.Results
                        currentBitmap = bitmap
                        imageRect = rect
                    }
                )
            }
            ScreenState.LoadImage -> {
                LoadImage(
                    currentBitmap!!
                )
            }
            ScreenState.Results -> {
                currentBitmap?.let { Results(bitmap = it, imageRect = imageRect) }
            }
        }
    }
}

