package ru.naviai.aiijc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


@Throws(IOException::class)
fun assetFilePath(context: Context, assetName: String?): String? {
    val file = File(context.filesDir, assetName.toString())
    if (file.exists() && file.length() > 0) {
        return file.absolutePath
    }
    context.assets.open(assetName!!).use { `is` ->
        FileOutputStream(file).use { os ->
            val buffer = ByteArray(4 * 1024)
            var read: Int
            while (`is`.read(buffer).also { read = it } != -1) {
                os.write(buffer, 0, read)
            }
            os.flush()
        }
        return file.absolutePath
    }
}


class ModelResults (
    val count: Int,
    val bitmap: Bitmap
)

class Model(context: Context) {
    private val model: Module

    init {
        model = Module.load(assetFilePath(context, "uv.ptl"))
    }

    fun predict(
        bitmap: Bitmap
    ): ModelResults {
        Log.i("kilo", bitmap.width.toString())
        Log.i("kilo", bitmap.height.toString())

        val imageTensor = TensorImageUtils.bitmapToFloat32Tensor(
            bitmap,
            floatArrayOf(0.0f, 0.0f, 0.0f),
            floatArrayOf(1.0f, 1.0f, 1.0f)
        )

        Log.i("kilo", imageTensor.shape().toString())
        Log.i("kilo", imageTensor.shape().size.toString())
        Log.i("kilo", imageTensor.shape()[0].toString())
        Log.i("kilo", imageTensor.shape()[1].toString())
        Log.i("kilo", imageTensor.shape()[2].toString())
        Log.i("kilo", imageTensor.shape()[3].toString())

        val inputValues = IValue.from(imageTensor)
        val output = model.forward(inputValues)
        val outputTensor = output.toTuple()[0].toTensor()

        return postprocessOutput(
            outputTensor,
            bitmap
        )
    }
}

private fun drawRectangleOnBitmap(bitmap: Bitmap, data: List<List<Float>>): Bitmap {
    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = android.graphics.Canvas(mutableBitmap)
    val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
    }

    for (box in data) {
        val x1 = box[0]
        val y1 = box[1]
        val x2 = box[2]
        val y2 = box[3]

        canvas.drawRect(x1, y1, x2, y2, paint)
    }

    return mutableBitmap
}


private fun postprocessOutput(tensor: Tensor, bitmap: Bitmap): ModelResults {
    val objects = processModelOutput(tensor, 0.6f, 0.45f)

    return ModelResults(objects.size, drawRectangleOnBitmap(bitmap, objects))
}

fun xywh2xyxy(x: FloatArray): FloatArray {
    val y = FloatArray(4)

    y[0] = x[0] - x[2] / 2
    y[1] = x[1] - x[3] / 2
    y[2] = x[0] + x[2] / 2
    y[3] = x[1] + x[3] / 2

    return y
}

fun iou(a: List<Float>, b: List<Float>): Float {
    val areaA = (a[2] - a[0]) * (a[3] - a[1])
    if (areaA <= 0.0f) {
        return 0.0f
    }

    val areaB = (b[2] - b[0]) * (b[3] - b[1])
    if (areaB <= 0.0f) {
        return 0.0f
    }

    val intersectionMinX = a[0].coerceAtLeast(b[0])
    val intersectionMinY = a[1].coerceAtLeast(b[1])
    val intersectionMaxX = a[2].coerceAtMost(b[2])
    val intersectionMaxY = a[3].coerceAtMost(b[3])

    val intersectionArea = 0f.coerceAtLeast(intersectionMaxY - intersectionMinY) *
            0f.coerceAtLeast(intersectionMaxX - intersectionMinX)

    return intersectionArea / (areaA + areaB - intersectionArea)
}

fun nmsBoxes(boxes: List<List<Float>>, scores: List<Float>, threshold: Float): List<List<Float>> {
    val values = boxes.mapIndexed { index, box -> box + listOf(scores[index], index.toFloat()) }

    val sortedValues = values.sortedByDescending { it[4] }.toMutableList()

    val result = mutableListOf<List<Float>>()

    while (sortedValues.isNotEmpty()) {
        result.add(sortedValues[0].subList(0, 4) + listOf(scores[0], 0f))

        sortedValues.removeAll { box ->
            (iou(box, sortedValues[0]) > threshold) || isInside(box, sortedValues[0])
        }
    }

    return result
}

private fun isInside(inner: List<Float>, outer: List<Float>): Boolean {
    return inner[0] > outer[0] && inner[1] > outer[1] && inner[2] < outer[2] && inner[3] < outer[3]
}

private fun processModelOutput(
    tensor: Tensor,
    confThres: Float,
    iouThres: Float,
): List<List<Float>> {
    val result = ArrayList<ArrayList<Float>>()
    val boxes = ArrayList<FloatArray>()
    val scores = ArrayList<Float>()

    val values = tensor.dataAsFloatArray

    var counter = 0

    for (i in 0..<(values.size / 6)) {
        counter += 1
        if (values[i * 6 + 4] < confThres) continue

        values[i * 6 + 5] *= values[i * 6 + 4]
        val box = xywh2xyxy(values.copyOfRange(i * 6, i * 6 + 4))

        val conf = values[i * 6 + 5]

        boxes.add(box)
        scores.add(conf)

        result.add(ArrayList(listOf(box[0], box[1], box[2], box[3], conf, 0f)))
    }

    return nmsBoxes(result, scores, iouThres)
}