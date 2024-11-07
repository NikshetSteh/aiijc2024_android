package ru.naviai.aiijc.ui.fragments

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.FiltersParams
import ru.naviai.aiijc.ImageRect
import ru.naviai.aiijc.adjustBitmap


@Composable
fun Filters(
    bitmap: Bitmap,
    imageRect: ImageRect,
    onReady: (filtersParams: FiltersParams) -> Unit,
    startFiltersParams: FiltersParams?
) {
    var currentFilters by remember {
        mutableStateOf(
            startFiltersParams
                ?: FiltersParams(
                    brightness = 0f,
                    saturation = 1f,
                    iou = 0.6f,
                    threshold = 0.2f,
                    sharpness = 0f
                )
        )
    }

    var currentImage by remember { mutableStateOf(bitmap) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    imageRect.imageOffset
                },
            contentAlignment = androidx.compose.ui.Alignment.TopCenter
        ) {
            Image(
                currentImage.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.FillWidth
            )
        }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    Icons.Filled.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = {
                }
            ) {
                Icon(
                    Icons.Filled.ExitToApp,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = androidx.compose.ui.Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.title_brightness),
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                    Slider(
                        value = (currentFilters.brightness + 100) / 200,
                        onValueChange = {
                            currentFilters = currentFilters.copy(brightness = it * 200 - 100)
                        }
                    )

                    Text(
                        stringResource(R.string.title_sharpness),
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                    Slider(
                        value = currentFilters.sharpness / 100,
                        onValueChange = {
                            currentFilters = currentFilters.copy(sharpness = it * 100)
                        }
                    )

                    Text(
                        stringResource(R.string.title_saturation),
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                    Slider(
                        value = currentFilters.saturation / 7,
                        onValueChange = {
                            currentFilters = currentFilters.copy(saturation = it * 7)
                        }
                    )

                    Text(
                        stringResource(R.string.title_threshold),
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                    Slider(
                        value = 1 - (currentFilters.threshold - 0.05f) / 0.45f,
                        onValueChange = {
                            currentFilters =
                                currentFilters.copy(threshold = (-it + 1) * 0.45f + 0.05f)
                        }
                    )

                    Text(
                        stringResource(R.string.title_iou),
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                    Slider(
                        value = (currentFilters.iou - 0.35f) / 0.4f,
                        onValueChange = {
                            currentFilters = currentFilters.copy(iou = it * 0.4f + 0.35f)
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            currentFilters = FiltersParams(
                                brightness = 0f,
                                saturation = 1f,
                                iou = 0.6f,
                                threshold = 0.2f,
                                sharpness = 0f
                            )
                        }) {
                            Text(stringResource(R.string.action_reset))
                        }
                        Button(onClick = {
                            onReady(
                                currentFilters
                            )
                        }) {
                            Text(stringResource(R.string.action_apply))
                        }
                    }
                }
            }
        }
    }

    var timeoutJob: Job? = null

    LaunchedEffect(currentFilters) {
        timeoutJob?.cancel()

        timeoutJob = launch {
            delay(200)
            currentImage = adjustBitmap(
                bitmap,
                currentFilters.brightness,
                currentFilters.saturation,
                currentFilters.sharpness
            )
        }
    }
}
