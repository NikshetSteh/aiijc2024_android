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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
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
    var isReady by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }

    val context = LocalContext.current
    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)

    val bitmap = BitmapFactory.decodeStream(inputStream)

    val model = LiteModuleLoader.load(assetFilePath(LocalContext.current, "uu.ptl"))

    var isLoading by remember { mutableStateOf(false) }
    var resultBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        resultBitmap = loadImageAndPredictAsync(bitmap, model)
        Log.i("kilo", "checker")
        isLoading = false
    }


//    if (!isReady) {
//        val py = Python.getInstance()
//        val pyobj = py.getModule("main")
//        val obj = pyobj.callAttr("main", imageUri.toString())
//        result = obj.toString()
//        isReady = true
//    }
//
//    val context = LocalContext.current
//    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
//
//    val bitmap = BitmapFactory.decodeStream(inputStream).asImageBitmap()
//
//    Box {
//        Image(
//            bitmap,
//            "",
//            modifier = Modifier.padding(4.dp)
//        )
//
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .align(Alignment.BottomCenter)
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceAround
//        ) {
//            Text(
//                "Кол-во труб: $result",
//                color = Color.White
//            )
//            Button(
//                onClick = { navController.navigate("home") },
//            ) {
//                Text("Назад")
//            }
//        }
//    }
}


private suspend fun loadImageAndPredictAsync(bitmap: Bitmap, model: Module): Bitmap {
    return withContext(Dispatchers.IO) {
        // Preprocess the image (resize, normalize, etc.)
        val inputTensor = preprocessImage(bitmap)

        // Make prediction
        val inputValues = IValue.from(inputTensor)
        val output = model.forward(inputValues)
        Log.i("kilo", output.toTuple()[0].toString())
        val outputTensor = output.toTuple()[0].toTensor()
        val prediction = output.toTuple()[0].toTensor().dataAsFloatArray.size

        // Process output to Bitmap (assuming segmentation)
//        postprocessOutput(outputTensor, bitmap.width, bitmap.height)
        bitmap
    }
}

private fun preprocessImage(bitmap: Bitmap): Tensor {
    // Resize the image
    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true)

    val imgTensor = FloatArray(640 * 640 * 3) // Assuming 3 channels (RGB)
    for (y in 0 until 640) {
        for (x in 0 until 640) {
            val pixel = resizedBitmap.getPixel(x, y)
            imgTensor[(y * 640 + x) * 3] = Color.red(pixel) / 255.0f // R
            imgTensor[(y * 640 + x) * 3 + 1] = Color.green(pixel) / 255.0f // G
            imgTensor[(y * 640 + x) * 3 + 2] = Color.blue(pixel) / 255.0f // B
        }
    }

    return Tensor.fromBlob(imgTensor, longArrayOf(1, 3, 640, 640))
}

private fun postprocessOutput(tensor: Tensor, width: Int, height: Int): Bitmap {
    // Create a Bitmap with the specified dimensions
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val tensorValue = tensor.dataAsFloatArray



    return bitmap
}


