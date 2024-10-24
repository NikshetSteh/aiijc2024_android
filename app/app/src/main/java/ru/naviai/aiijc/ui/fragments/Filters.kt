package ru.naviai.aiijc.ui.fragments

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay


class FiltersParams(
    var sharpness: Float, var brightness: Float, var contrast: Float
)


@Composable
fun Filters(
    bitmap: Bitmap
) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        modifier = Modifier
            .width(320.dp)
            .height(320.dp),
    )
}


@Composable
fun FiltersBottom(
    state: FiltersParams,
    onChange: (filtersParams: FiltersParams) -> Unit
) {
//    Column {
        Slider(
            value = state.sharpness,
            onValueChange = {
                state.sharpness = it
            },
            modifier = Modifier.width(250.dp)
        )

//        Slider(
//            value = state.brightness,
//            onValueChange = {
//                state.brightness = it
//
//            },
//            modifier = Modifier.width(250.dp)
//        )
//
//        Slider(
//            value = state.contrast,
//            onValueChange = {
//                state.contrast = it
//            },
//            modifier = Modifier.width(250.dp)
//        )
//    }

//    return filtersParams
}