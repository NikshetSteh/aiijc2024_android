package ru.naviai.aiijc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ImageRect(
    val imageOffset: IntOffset,
    val imageSize: IntOffset,
    val contentOffset: IntOffset? = null,
    val contentSize: IntOffset? = null,
    val o1: Offset? = null,
    val o2: Offset? = null,
    val cropImageSize: IntOffset? = null,
    val cropZonePadding: IntOffset? = null
)


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



fun makePrediction(
    model: Model,
    bitmap: Bitmap,
    onResult: (ModelResults) -> Unit,
    type: Model.PredictionsType,
    iou: Float,
    threshold: Float
) {
    CoroutineScope(Dispatchers.Main).launch {
        val result = withContext(Dispatchers.IO) {
            model.predict(
                bitmap,
                when (type) {
                    Model.PredictionsType.CIRCLE -> {
                        listOf(0)
                    }

                    Model.PredictionsType.QUAD -> {
                        listOf(1)
                    }

                    Model.PredictionsType.RECTANGLE -> {
                        listOf(2)
                    }
                },
                iou,
                threshold
            )
        }
        onResult(result)
    }
}

