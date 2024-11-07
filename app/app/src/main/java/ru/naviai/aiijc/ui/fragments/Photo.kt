package ru.naviai.aiijc.ui.fragments

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.CameraPreview
import ru.naviai.aiijc.ImageRect
import ru.naviai.aiijc.IntOffsetSerializable
import ru.naviai.aiijc.loadHistory
import ru.naviai.aiijc.takePhoto
import ru.naviai.aiijc.ui.EditRectangle
import ru.naviai.aiijc.ui.SelectField
import kotlin.math.roundToInt


@Composable
fun Photo(
    onMenu: () -> Unit,
    onLoad: (Bitmap, String) -> Unit = { _, _ -> },
    onCapture: (Bitmap, ImageRect, String) -> Unit = { _, _, _ -> },
    startType: String
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val screenWidth = configuration.screenWidthDp

    val verticalPaddings = 64
    val horizontalPaddings = 16

    var isLoading by remember { mutableStateOf(false) }

    var type by remember { mutableStateOf(startType) }

    var focus by remember { mutableStateOf<Offset?>(null) }

    val height = screenHeight / 4f * 3 - verticalPaddings * 2
    val width = screenWidth - horizontalPaddings * 2

    var flashLight by remember { mutableStateOf(false) }

    val context = LocalContext.current

    loadHistory(context)

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

        if (bitmap != null) {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)

            onLoad(bitmap!!, type)
        } else {
            isLoading = false
        }
    }

    var sizeX: Float
    var sizeY: Float

    var size by remember {
        mutableStateOf(Offset(0f, 0f))
    }

    with(LocalDensity.current) {
        sizeX = LocalConfiguration.current.screenWidthDp.dp.toPx()
        sizeY = LocalConfiguration.current.screenHeightDp.dp.toPx()
    }

    Box(
        contentAlignment = androidx.compose.ui.Alignment.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        val imageCapture = CameraPreview(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { clickOffset ->
                        focus = clickOffset
                        Log.i("kilo", "Tester click")

                    }
                },
            flashLight = flashLight,
            focus = if (focus!= null) {val buf = focus; focus = null; buf } else null
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val o1 = Offset(
                (screenWidth.dp.toPx() - size.x) / 2f,
                (screenHeight.dp.toPx() * 3 / 8 - size.y / 2)
            )
            val o2 = Offset(
                size.x,
                size.y
            )

            drawRect(
                color = Color.Black.copy(alpha = 0.5f), // Transparent black
                size = Size(
                    o1.x,
                    size.y
                ),
                topLeft = Offset(
                    0f,
                    o1.y
                )
            )

            drawRect(
                color = Color.Black.copy(alpha = 0.5f), // Transparent black
                size = Size(
                    this.size.width - o1.x - o2.x,
                    size.y
                ),
                topLeft = Offset(
                    o1.x + o2.x,
                    o1.y
                )
            )


            drawRect(
                color = Color.Black.copy(alpha = 0.5f), // Transparent black
                size = Size(
                    this.size.width,
                    o1.y
                ),
                topLeft = Offset.Zero
            )


            drawRect(
                color = Color.Black.copy(alpha = 0.5f), // Transparent black
                size = Size(
                    this.size.width,
                    this.size.height - o1.y - o2.y
                ),
                topLeft = Offset(
                    0f,
                    o1.y + o2.y
                )
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.TopStart
        ) {
            Row (
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onMenu,
                    enabled = !isLoading
                ) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = {
                    flashLight = !flashLight
                },
                    enabled = !isLoading
                ) {
                    Icon(
                        painter = painterResource(R.drawable.history),
                        contentDescription = "History",
                        tint = Color.White
                    )
                }
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
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
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
                    ),
                    onChange = {
                        type = it
                    },
                    value = type
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = {
                            isLoading = true
                            launcher.launch("image/*")
                        },
                        enabled = !isLoading
                    ) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.upload),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            takePhoto(imageCapture, context) {
                                isLoading = true

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
                                        IntOffsetSerializable(0, 0),
                                        IntOffsetSerializable(size.x.roundToInt(), size.y.roundToInt()),
                                    ),
                                    type
                                )
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ellipse),
                            contentDescription = null,
                        )
                    }

                    IconButton(
                        onClick = { flashLight = !flashLight },
                        enabled = !isLoading
                    ) {
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

