package ru.naviai.aiijc.ui.fragments

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.CameraPreview
import ru.naviai.aiijc.scaleBitmapWithBlackMargins

@Composable
fun PhotoTop(
    flashLight: Boolean = false
): ImageCapture {
    return CameraPreview(
        modifier = Modifier
            .width(320.dp)
            .height(320.dp),
        flashLight = flashLight
    )
}

@Composable
fun PhotoBottom(
    imageCapture: ImageCapture,
    flashLight: Boolean,
    onCapture: (Bitmap) -> Unit,
    onFlashLight: () -> Unit
) {
    val context = LocalContext.current

    var bitmap: Bitmap?

    Button(
        onClick = {
            imageCapture.takePicture(
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        super.onCaptureSuccess(image)

                        val matrix = Matrix().apply {
                            postRotate(image.imageInfo.rotationDegrees.toFloat())
                        }
                        bitmap = Bitmap.createBitmap(
                            image.toBitmap(),
                            0,
                            0,
                            image.width,
                            image.height,
                            matrix,
                            true
                        )
                        bitmap = scaleBitmapWithBlackMargins(
                            bitmap!!,
                            640,
                            640
                        )

                        onCapture(bitmap!!)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        super.onError(exception)
                        Log.e("Camera", "Couldn't take photo: ", exception)
                    }
                }
            )
        }
    ) {
        Text(text = "Take photo")
    }
    IconButton(onClick = onFlashLight) {
        Icon(
            painterResource(
                id = if (flashLight) R.drawable.flashlight_on else R.drawable.flashlight_off
            ),
            contentDescription = "Flashlight"
        )
    }
}