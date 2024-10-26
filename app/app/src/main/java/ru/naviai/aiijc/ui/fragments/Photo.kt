package ru.naviai.aiijc.ui.fragments

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.CameraPreview
import ru.naviai.aiijc.ImageRect
import ru.naviai.aiijc.takePhoto
import ru.naviai.aiijc.ui.EditRectangle
import ru.naviai.aiijc.ui.SelectField
import kotlin.math.roundToInt


@Composable
fun Photo(
    onMenu: () -> Unit,
    onLoad: (Bitmap) -> Unit = {},
    onCapture: (Bitmap, ImageRect, String) -> Unit = { _, _, _ -> }
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val screenWidth = configuration.screenWidthDp

    val verticalPaddings = 64
    val horizontalPaddings = 16
    val startType = stringResource(R.string.type_circle)

    var type by remember { mutableStateOf(startType) }

    val height = screenHeight / 4f * 3 - verticalPaddings * 2
    val width = screenWidth - horizontalPaddings * 2

    var flashLight by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract =
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        var bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            uri?.let {
                ImageDecoder.createSource(
                    context.contentResolver,
                    it
                )
            }?.let { ImageDecoder.decodeBitmap(it) }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        bitmap = bitmap?.copy(Bitmap.Config.ARGB_8888, false)

        onLoad(bitmap!!)
    }

    var sizeX: Float
    var sizeY: Float

    var size = Offset(0f, 0f)

    with(LocalDensity.current) {
        sizeX = LocalConfiguration.current.screenWidthDp.dp.toPx()
        sizeY = LocalConfiguration.current.screenHeightDp.dp.toPx()
    }

    Box(
        contentAlignment = androidx.compose.ui.Alignment.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        val imageCapture = CameraPreview(
            modifier = Modifier.fillMaxSize(),
            flashLight = flashLight
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.TopStart
        ) {
            IconButton(onClick = onMenu) {
                Icon(Icons.Filled.Menu, contentDescription = null, tint = Color.White)
            }
        }

        with(LocalDensity.current) {
            Box(
                modifier = Modifier.offset(y = (-(screenHeight / 8)).dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = null,
                    tint = Color.White
                )
                size = EditRectangle(
                    90.dp.toPx(),
                    height.dp.toPx(),
                    90.dp.toPx(),
                    width.dp.toPx()
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Column(
                modifier = Modifier.height((screenHeight / 3).dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                SelectField(
                    modifier = Modifier.width(200.dp),
                    label = stringResource(R.string.label_type),
                    options = listOf(
                        stringResource(R.string.type_circle),
                        stringResource(R.string.type_rectangle),
                        stringResource(R.string.type_quad),
                    ),
                    onChange = {
                        type = it
                    },
                    value = type
                )

                Row(
                    modifier = Modifier.width((screenWidth / 2).dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = {
                        takePhoto(imageCapture, context) {
                            launcher.launch("image/*")
                        }
                    }) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.upload),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    IconButton(onClick = {
                        takePhoto(imageCapture, context) {
                            val imageHeight = it.height.toFloat()
                            val imageWidth = sizeX * (it.height.toFloat() / sizeY)

                            val cropped = Bitmap.createBitmap(
                                it,
                                ((it.width - imageWidth) / 2).roundToInt(),
                                0,
                                imageWidth.roundToInt(),
                                imageHeight.roundToInt()
                            )

                            val resized = Bitmap.createScaledBitmap(
                                cropped,
                                sizeX.roundToInt(),
                                sizeY.roundToInt(),
                                true
                            )

                            onCapture(
                                resized,
                                ImageRect(
                                    IntOffset(0, 0),
                                    IntOffset(size.x.roundToInt(), size.y.roundToInt()),
                                ),
                                type
                            )
                        }
                    }) {
                        Image(
                            painter = painterResource(R.drawable.ellipse),
                            contentDescription = null,
                        )
                    }

                    IconButton(onClick = { flashLight = !flashLight }) {
                        Icon(
                            if (flashLight)
                                ImageVector.vectorResource(R.drawable.flashlight_on)
                            else
                                ImageVector.vectorResource(R.drawable.flashlight_off),
                            contentDescription = null,
                            tint = Color.White,
                        )
                    }
                }
            }
        }
    }
}

