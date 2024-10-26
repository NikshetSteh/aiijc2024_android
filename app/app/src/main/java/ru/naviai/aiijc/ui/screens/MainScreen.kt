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
import kotlinx.coroutines.launch
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.ImageRect
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

    var type by remember {
        mutableStateOf(resources.getString(R.string.type_circle))
    }

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
                    onCapture = { bitmap, rect, newType ->
                        state = ScreenState.Results
                        currentBitmap = bitmap
                        imageRect = rect
                        type = newType
                    }
                )
            }
            ScreenState.LoadImage -> {
                LoadImage(
                    currentBitmap!!,
                    onReady = { bitmap, rect, newType ->
                        state = ScreenState.Results
                        currentBitmap = bitmap
                        imageRect = rect
                        type = newType
                    }
                )
            }
            ScreenState.Results -> {
                currentBitmap?.let { Results(it, imageRect, type, onBack = { state = ScreenState.Camera }) }
            }
        }
    }
}

