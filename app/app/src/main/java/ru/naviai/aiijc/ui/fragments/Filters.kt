package ru.naviai.aiijc.ui.fragments

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.adjustBitmap


class FiltersParams(
    val sharpness: Float, val brightness: Float, val contrast: Float
)

@Composable
fun Filters(
    initialBitmap: Bitmap,
    lastParams: FiltersParams?,
    ready: (bitmap: Bitmap, filtersParams: FiltersParams) -> Unit,
    back: () -> Unit
) {
    val resource = LocalContext.current.resources

    val bitmap by remember {
        mutableStateOf(initialBitmap)
    }
    var modifiedBitmap by remember {
        mutableStateOf(initialBitmap)
    }

    var filtersParams by remember {
        mutableStateOf(
            lastParams ?: FiltersParams(0f, 0f, 1f)
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            bitmap = applyFilters(bitmap, filtersParams).asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .width(320.dp)
                .height(320.dp),
        )

        Column(
            modifier = Modifier.width(250.dp)
        ) {
//            Row (
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(resource.getString(R.string.title_sharpness))
//                Spacer(modifier = Modifier.width(16.dp))
//                Slider(
//                    value = filtersParams.sharpness / 100f + 0.5f,
//                    onValueChange = {
//                        filtersParams =
//                            FiltersParams(
//                                (it - 0.5f) * 100f,
//                                filtersParams.brightness,
//                                filtersParams.contrast
//                            )
//                    }
//                )
//            }

            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(resource.getString(R.string.title_brightness))
                Spacer(modifier = Modifier.width(16.dp))
                Slider(
                    value = filtersParams.brightness / 100f + 0.5f,
                    onValueChange = {
                        filtersParams =
                            FiltersParams(
                                filtersParams.sharpness,
                                (it - 0.5f) * 100f,
                                filtersParams.contrast
                            )
                    }
                )
            }

            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(resource.getString(R.string.title_saturation))
                Spacer(modifier = Modifier.width(16.dp))
                Slider(
                    value = filtersParams.contrast / 100f + 0.5f,
                    onValueChange = {
                        filtersParams =
                            FiltersParams(
                                filtersParams.sharpness,
                                filtersParams.brightness,
                                (it - 0.5f) * 100f
                            )
                    }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = back) {
                Text(resource.getString(R.string.action_back))
            }

            Button(onClick = {
                ready(modifiedBitmap, filtersParams)
            }) {
                Text(resource.getString(R.string.action_apply))
            }
        }



        Button(onClick = {
            filtersParams = FiltersParams(0f, 0f, 1f)
        }) {
            Text(resource.getString(R.string.action_reset))
        }

    }

    LaunchedEffect(filtersParams) {
        while (true) {
            modifiedBitmap = applyFilters(bitmap, filtersParams)

            delay(3000)
        }
    }
}

fun applyFilters(
    bitmap: Bitmap,
    filtersParams: FiltersParams
): Bitmap {
    return adjustBitmap(bitmap, filtersParams.brightness, filtersParams.contrast)
//    return applySharpeningFilter(buffer, filtersParams.sharpness)
}