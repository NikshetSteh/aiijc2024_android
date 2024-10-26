package ru.naviai.aiijc.ui.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
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
import androidx.compose.material.icons.filled.AddCircle
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.CameraPreview
import ru.naviai.aiijc.ui.Rectangle


@Composable
fun Photo(
    onMenu: () -> Unit,
    onLoad: (Bitmap) -> Unit = {},
    onCapture: (Bitmap) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val screenWidth = configuration.screenWidthDp

    val verticalPaddings = 64
    val horizontalPaddings = 32

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
                Icon(Icons.Filled.Menu, contentDescription = null)
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
                )
                Rectangle(
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
                IconButton(onClick = { takePhoto(imageCapture, context, onCapture) }) {
                    Icon(
                        Icons.Filled.AddCircle,
                        modifier = Modifier
                            .height(256.dp)
                            .width(256.dp),
                        contentDescription = null
                    )
                }
                Row(
                    modifier = Modifier.width((screenWidth / 2).dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = {
                        takePhoto(imageCapture, context) {
                            launcher.launch("image/*")
                        }
                    }) {
                        Icon(painterResource(R.drawable.upload), contentDescription = null)
                    }
                    IconButton(onClick = { flashLight = !flashLight }) {
                        Icon(
                            if (flashLight)
                                painterResource(R.drawable.flashlight_on)
                            else
                                painterResource(R.drawable.flashlight_off),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}


fun takePhoto(imageCapture: ImageCapture, context: Context, onCapture: (Bitmap) -> Unit) {
    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)

                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                val bitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0,
                    0,
                    image.width,
                    image.height,
                    matrix,
                    true
                )

                onCapture(bitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Couldn't take photo: ", exception)
            }
        }
    )
}
