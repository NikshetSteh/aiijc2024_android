package ru.naviai.aiijc.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils.bitmapToFloat32Tensor
import ru.naviai.aiijc.PrePostProcessor.outputsToNMSPredictions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


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


@Composable
fun ResultScreen(imageUri: Uri, navController: NavController) {
//    var isReady by remember { mutableStateOf(false) }
//    var result by remember { mutableStateOf("") }

    val context = LocalContext.current
    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)

    val bitmap = BitmapFactory.decodeStream(inputStream)

    val model = Module.load(assetFilePath(LocalContext.current, "uv.ptl"))

    var isLoading by remember { mutableStateOf(false) }
    var resultBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        resultBitmap = loadImageAndPredictAsync(bitmap, model, context)
        Log.i("kilo", "checker")
        isLoading = false
    }
}


private suspend fun loadImageAndPredictAsync(bitmap: Bitmap, model: Module, context: Context): Bitmap {
    return withContext(Dispatchers.IO) {
        // Preprocess the image (resize, normalize, etc.)
        val inputTensor = preprocessImage(bitmap, context)

        // Make prediction
        val inputValues = IValue.from(inputTensor)
        val output = model.forward(inputValues)
        Log.i("kilo", output.toTuple()[0].toString())
        val outputTensor = output.toTuple()[0].toTensor()

//        val prediction = output.toTuple()[0].toTensor().dataAsFloatArray

        postprocessOutput(
            outputTensor,
            bitmap.width,
            bitmap.height
        )

        // Process output to Bitmap (assuming segmentation)
//        postprocessOutput(outputTensor, bitmap.width, bitmap.height)
        bitmap
    }
}

fun tensorToBitmap(imageTensor: Tensor): Bitmap {
    // Check if the input tensor has the expected shape
    val height = 640
    val width = 640

    // Create a Bitmap with the specified width and height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val values = imageTensor.dataAsFloatArray

    // Iterate over each pixel and set the color in the Bitmap
    for (h in 0 until height) {
        for (w in 0 until width) {
            // Get the pixel values for each channel (assuming values are in range [0, 1])
            val r = (values[640 * 640 * 0 + 640 * h + w] * 255).toInt().coerceIn(0, 255)
            val g = (values[640 * 640 * 1 + 640 * h + w] * 255).toInt().coerceIn(0, 255)
            val b = (values[640 * 640 * 2 + 640 * h + w] * 255).toInt().coerceIn(0, 255)

            // Set the pixel color in the Bitmap
            bitmap.setPixel(w, h, Color.rgb(r, g, b))
        }
    }

    return bitmap
}

private fun preprocessImage(bitmap: Bitmap, context: Context): Tensor {
    // Resize the image
    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true)

//    val file = File(context.cacheDir, "temp_image.png")
    val file = File(context.cacheDir, "result_image.png")
    val outputStream = FileOutputStream(file)
    resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    outputStream.flush()
    outputStream.close()

//    val imgTensor = FloatArray(640 * 640 * 3) // Assuming 3 channels (RGB)
//    for (y in 0 until 640) {
//        for (x in 0 until 640) {
//            val pixel = resizedBitmap.getPixel(x, y)
//            imgTensor[(y * 640 + x) * 3] = Color.red(pixel) / 255.0f // R
//            imgTensor[(y * 640 + x) * 3 + 1] = Color.green(pixel) / 255.0f // G
//            imgTensor[(y * 640 + x) * 3 + 2] = Color.blue(pixel) / 255.0f // B
//        }
//    }

    val buffer = bitmapToFloat32Tensor(
        resizedBitmap,
        floatArrayOf(0.0f, 0.0f, 0.0f),
        floatArrayOf(1.0f, 1.0f, 1.0f)
    )

    val file2 = File(context.cacheDir, "result_image2.png")
    val outputStream2 = FileOutputStream(file2)
    tensorToBitmap(buffer).compress(Bitmap.CompressFormat.PNG, 100, outputStream2)
    outputStream2.flush()
    outputStream2.close()

    val shape = buffer.shape()
    Log.i("kilo", "buffer: ${shape.size} ${shape[0]} ${shape[1]} ${shape[2]} ${shape[3]}")

    return buffer
}

private fun postprocessOutput(tensor: Tensor, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val tensorValue = tensor.dataAsFloatArray
    val imgScaleX: Float = bitmap.width.toFloat() / 640
    val imgScaleY: Float = bitmap.height.toFloat() / 640
    val ivScaleX = width.toFloat() / bitmap.width
    val ivScaleY = height.toFloat() / bitmap.height

    val results = outputsToNMSPredictions(
        tensorValue,
        imgScaleX,
        imgScaleY,
        ivScaleX,
        ivScaleY,
        0f,
        0f
    )

    Log.i("kilo", "results: ${results.size}")

    processModelOutput(tensor, 0.6f, 0.45f)

    return bitmap
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
    // Create a list of values combining boxes and scores with their indices
    val values = boxes.mapIndexed { index, box -> box + listOf(scores[index], index.toFloat()) }

    // Sort values by score in descending order
    val sortedValues = values.sortedByDescending { it[4] }.toMutableList()

    val result = mutableListOf<List<Float>>()

    while (sortedValues.isNotEmpty()) {
        // Add the highest score box to the result
        result.add(sortedValues[0].subList(0, 4) + listOf(scores[0], 0f))

        // Remove boxes that have IoU greater than the threshold with the first box
        sortedValues.removeAll { box ->
            (iou(box, sortedValues[0]) > threshold) || isInside(box, sortedValues[0])
        }

        // Remove the first box from the sorted list
//        if (sortedValues.isNotEmpty()) {
//            sortedValues.removeAt(0)
//        }
    }

    return result
}

private fun isInside(inner: List<Float>, outer: List<Float>): Boolean {
    return inner[0] > outer[0] && inner[1] > outer[1] && inner[2] < outer[2] && inner[3] < outer[3]
}

private fun processModelOutput(
    tensor: Tensor,
    conf_thres: Float = 0.6f,
    iou_thres: Float = 0.45f,
) {
    val result = ArrayList<ArrayList<Float>>()
    val boxes = ArrayList<FloatArray>()
    val scores = ArrayList<Float>()

    val values = tensor.dataAsFloatArray

    Log.i("kilo", "Check in 0 ${values.size}")

    var counter = 0

    for (i in 0..<(values.size/6)) {
        counter += 1
        if (values[i * 6 + 4] < conf_thres) continue

        values[i * 6 + 5] *= values[i * 6 + 4]
        val box = xywh2xyxy(values.copyOfRange(i * 6, i * 6 + 4))

        val conf = values[i * 6 + 5]

        boxes.add(box)
        scores.add(conf)

        result.add(ArrayList(listOf(box[0], box[1], box[2], box[3], conf, 0f)))
    }

    Log.i("kilo", "Counter is $counter")

    Log.i("kilo", "Check out1: ${result.size}")

    val output = nmsBoxes(result, scores, iou_thres)

    Log.i("kilo", "Check out2: ${output.size}")

    for (box in output) {
        Log.i("kilo", "Box: ${box[0]} ${box[1]} ${box[2]} ${box[3]} ${box[4]} ${box[5]}")
    }
}

