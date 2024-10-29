package ru.naviai.aiijc.ui.fragments

import android.graphics.Bitmap
import android.util.Log
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
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.FiltersParams
import ru.naviai.aiijc.ImageRect
import ru.naviai.aiijc.adjustBitmap


@Composable
fun Filters(
    bitmap: Bitmap,
    imageRect: ImageRect,
    onReady: (filtersParams: FiltersParams) -> Unit
) {
    var brightness by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(1f) }
    var iouCoefficient by remember { mutableStateOf(0.6f) }
    var thresholdCoefficient by remember { mutableStateOf(0.2f) }
    var sharpness by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
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
                (adjustBitmap(bitmap, brightness, saturation, sharpness)).asImageBitmap(),
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
                    Text(stringResource(R.string.title_brightness), fontSize = MaterialTheme.typography.bodySmall.fontSize)
                    Slider(
                        value = (brightness + 100) / 200,
                        onValueChange = {
                            brightness = it * 200 - 100
                        }
                    )

                    Text(stringResource(R.string.title_sharpness), fontSize = MaterialTheme.typography.bodySmall.fontSize)
                    Slider(
                        value = sharpness / 100,
                        onValueChange = {
                            sharpness = it * 100
                        }
                    )

                    Text(stringResource(R.string.title_saturation), fontSize = MaterialTheme.typography.bodySmall.fontSize)
                    Slider(
                        value = (saturation + 100) / 200,
                        onValueChange = {
                            saturation = it * 200 - 100
                        }
                    )

                    Text(stringResource(R.string.title_threshold), fontSize = MaterialTheme.typography.bodySmall.fontSize)
                    Slider(
                        value = 1-(thresholdCoefficient - 0.05f) / 0.45f,
                        onValueChange = {
                            thresholdCoefficient = (-it+1) * 0.45f + 0.05f
                            Log.i("kilo", "$thresholdCoefficient")
                        }
                    )

                    Text(stringResource(R.string.title_iou), fontSize = MaterialTheme.typography.bodySmall.fontSize)
                    Slider(
                        value = (iouCoefficient - 0.35f) / 0.4f,
                        onValueChange = {
                            iouCoefficient = it * 0.4f + 0.35f
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            brightness = 0f
                            saturation = 1f
                            iouCoefficient = 0.6f
                            thresholdCoefficient = 0.2f
                            sharpness = 0f
                        }) {
                            Text(stringResource(R.string.action_reset))
                        }
                        Button(onClick = {
                            onReady(
                                FiltersParams(
                                    brightness = brightness,
                                    saturation = saturation,
                                    iou = iouCoefficient,
                                    threshold = thresholdCoefficient,
                                    sharpness = sharpness
                                )
                            )
                        }) {
                            Text(stringResource(R.string.action_apply))
                        }
                    }
                }
            }
        }
    }
}

//
//fun applyFilters(
//    bitmap: Bitmap,
//    filtersParams: FiltersParams
//): Bitmap {
//    return adjustBitmap(bitmap, filtersParams.brightness, filtersParams.contrast)
//}