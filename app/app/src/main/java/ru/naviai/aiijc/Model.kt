package ru.naviai.aiijc

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.serialization.Serializable
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.abs
import kotlin.math.roundToInt


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


@Serializable
data class ModelResults(
    val count: Int,
    val items: List<Item>,
    val meanSize: OffsetSerializable,
)


@Serializable
class Item(
    val offset: IntOffsetSerializable
)


class Model(context: Context) {
    enum class PredictionsType {
        CIRCLE,
        RECTANGLE,
        ALL
    }


    private val model: Module

    init {
        model = Module.load(assetFilePath(context, "segmenter.torchscript"))
    }

    fun predict(
        bitmap: Bitmap,
        type: List<Int>,
        iouThres: Float,
        confThres: Float
    ): ModelResults {

        val imageTensor = TensorImageUtils.bitmapToFloat32Tensor(
            bitmap,
            floatArrayOf(0.0f, 0.0f, 0.0f),
            floatArrayOf(1.0f, 1.0f, 1.0f)
        )

        val inputValues = IValue.from(imageTensor)

        val output: IValue = model.forward(inputValues)
        val outputTensor = output.toTuple()[0].toTensor()

        return postprocessOutput(
            outputTensor,
            type,
            iouThres,
            confThres
        )
    }
}


private fun postprocessOutput(
    tensor: Tensor,
    type: List<Int>,
    iouThres: Float,
    confThres: Float
): ModelResults {
    val objects = processModelOutput(tensor, confThres, iouThres, type)

    var sizeSumX = 0f
    var sizeSumY = 0f

    val items = objects.map {
        sizeSumX += abs(it[2] - it[0])
        sizeSumY += abs(it[3] - it[1])
        Item(
            IntOffsetSerializable(
                ((it[0] + it[2]) / 2).roundToInt(),
                ((it[1] + it[3]) / 2).roundToInt()
            )
        )
    }

    return ModelResults(
        objects.size,
        items,
        if (objects.isNotEmpty())
            OffsetSerializable(
                sizeSumX / objects.size,
                sizeSumY / objects.size
            )
        else
            OffsetSerializable(40f, 40f)
    )
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

    val paddings = 38
    val size = values.size / paddings

    Log.i("kilo", "Size: $size")
    Log.i("kilo", "Values Size: ${values.size}")

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

        val conf: Float = values[i + (4 + resultClass) * size]

        if (conf < confThres) continue

        val box = xywh2xyxy(
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