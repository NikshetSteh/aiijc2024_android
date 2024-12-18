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
import kotlinx.serialization.Serializable


@Serializable
data class ImageRect(
    val imageOffset: IntOffsetSerializable,
    val imageSize: IntOffsetSerializable,
    val contentOffset: IntOffsetSerializable? = null,
    val contentSize: IntOffsetSerializable? = null,
    val o1: OffsetSerializable? = null,
    val o2: OffsetSerializable? = null,
    val cropImageSize: IntOffsetSerializable? = null,
    val cropZonePadding: IntOffsetSerializable? = null
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
    threshold: Float,
    initialBitmap: Bitmap = bitmap,
    context: Context,
    filters: FiltersParams,
    imageRect: ImageRect,
    needSave: Boolean
) {
    CoroutineScope(Dispatchers.Main).launch {
        val result = withContext(Dispatchers.IO) {
            model.predict(
                bitmap,
                when (type) {
                    Model.PredictionsType.CIRCLE -> {
                        listOf(0)
                    }

                    Model.PredictionsType.RECTANGLE -> {
                        listOf(1)
                    }

                    Model.PredictionsType.ALL -> {
                        listOf(0, 1)
                    }
                },
                iou,
                threshold
            )
        }


        Log.i("kilo", "Start saving")

        if (needSave) {
            saveResultsWithImageByDate(
                context = context,
                initialBitmap = initialBitmap,
                filteredBitmap = bitmap,
                item = HistoryItem(
                    modelResults = result,
                    filtersParams = filters,
                    imageRect = imageRect,
                    type = type
                )
            )
        }

        Log.i("kilo", "saved")
        onResult(result)
    }
}


@Serializable
class IntOffsetSerializable(
    var x: Int,
    var y: Int,
)

@Serializable
class OffsetSerializable(
    var x: Float,
    var y: Float,
)

fun OffsetSerializable.toOffset() = Offset(x, y)
fun IntOffsetSerializable.toOffset() = IntOffset(x, y)
fun IntOffset.toSerializable() = IntOffsetSerializable(x, y)
fun Offset.toSerializable() = OffsetSerializable(x, y)
