package ru.naviai.aiijc.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import ru.naviai.aiijc.CameraPreview
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


@Composable
fun CameraScreen(
    applicationContext: Context,
    navController: NavController,
    cameraController: LifecycleCameraController,
    modifier: Modifier = Modifier
) {
    Log.i("kilo", "Home screen")

    Box(modifier = modifier) {
        CameraPreview(
            controller = cameraController,
            modifier = Modifier
                .fillMaxSize()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = {
                takePhoto(
                    cameraController,
                    onPhotoTaken = {
                        val uri = it.toUri(applicationContext)

                        Log.i("kilo", "Photo is ready")
                        Log.i("kilo", uri.toString())

                        val encodedUrl = URLEncoder.encode(
                            uri.toString(),
                            StandardCharsets.UTF_8.toString()
                        )

                        navController.navigate("crop/$encodedUrl")
                    },
                    applicationContext
                )
            }) {

            }
        }
    }
}


private fun takePhoto(
    controller: LifecycleCameraController,
    onPhotoTaken: (Bitmap) -> Unit,
    applicationContext: Context
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(applicationContext),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)

                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0,
                    0,
                    image.width,
                    image.height,
                    matrix,
                    true
                )

                onPhotoTaken(rotatedBitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Couldn't take photo: ", exception)
            }
        }
    )
}

fun Bitmap.toUri(context: Context): Uri {
    val file = File(context.cacheDir, "tempImage.jpg")
    try {
        FileOutputStream(file).use { out ->
            this.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return Uri.fromFile(file)
}