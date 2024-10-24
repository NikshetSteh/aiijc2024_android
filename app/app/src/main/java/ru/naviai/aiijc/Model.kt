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


class ModelResults(
    val count: Int,
    val bitmap: Bitmap
)

class Model(context: Context) {
    enum class PredictionsType {
        CIRCLE,
        RECTANGLE,
        QUAD
    }


    private val model: Module
    private val roundModel: Module

    init {
        roundModel = Module.load(assetFilePath(context, "uv.ptl"))
        model = Module.load(assetFilePath(context, "b.torchscript"))
    }

    fun predict(
        bitmap: Bitmap,
        type: List<Int>
    ): ModelResults {

        val imageTensor = TensorImageUtils.bitmapToFloat32Tensor(
            bitmap,
            floatArrayOf(0.0f, 0.0f, 0.0f),
            floatArrayOf(1.0f, 1.0f, 1.0f)
        )

        val inputValues = IValue.from(imageTensor)
        val output: IValue = if (type.size == 1 && type[0] == 0) {
            roundModel.forward(inputValues)
        } else {
            model.forward(inputValues)
        }
        val outputTensor = if(type.size == 1 && type[0] == 0) output.toTuple()[0].toTensor() else output.toTensor()

        return postprocessOutput(
            outputTensor,
            bitmap,
            type
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


private fun postprocessOutput(tensor: Tensor, bitmap: Bitmap, type: List<Int>): ModelResults {
    val objects = processModelOutput(tensor, 0.6f, 0.4f, type)

    return ModelResults(objects.size, drawRectangleOnBitmap(bitmap, objects))
}

fun xywh2xyxy(x: List<Float>): FloatArray {
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

        val buffer = sortedValues[0]

        sortedValues.removeAll { box ->
            (iou(box, buffer) > threshold) || isInside(box, buffer) || isInside(buffer, box)
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
    ids: List<Int>
): List<List<Float>> {

    Log.i("kilo", "processModelOutput")

    val result = ArrayList<ArrayList<Float>>()
    val boxes = ArrayList<FloatArray>()
    val scores = ArrayList<Float>()

    val values = tensor.dataAsFloatArray

    val paddings = if (ids.size > 1 || ids[0] != 0) 7 else 6
    val size = values.size / paddings

    var counter = 0

    for (i in 0..<(size)) {
        counter += 1

        var resultClass = ids[0]
        if (ids.size > 1) {
            for (j in ids) {
                resultClass =
                    if (values[i + (4 + j) * size] > values[i + (4 + resultClass) * size]) j else resultClass
            }
        }

        val conf: Float = if (ids.size == 1 && ids[0] == 0) {
            values[i * 6 + 5] * values[i * 6 + 4]
        }else {
            values[i + (4 + resultClass) * size]
        }

        if (conf < confThres) continue

        val box = xywh2xyxy(
            if (ids.size == 1 && ids[0] == 0)
                listOf(
                    values[i * 6],
                    values[i * 6 + 1],
                    values[i * 6 + 2],
                    values[i * 6 + 3],
                )
                else
            listOf(
                values[i],
                values[i + 1 * size],
                values[i + 2 * size],
                values[i + 3 * size],
            )
        )

        boxes.add(box)
        scores.add(conf)

        result.add(ArrayList(listOf(box[0], box[1], box[2], box[3], conf, 0f)))
    }

    Log.i("kilo", "Box count: $counter")

    return nmsBoxes(result, scores, iouThres)
}