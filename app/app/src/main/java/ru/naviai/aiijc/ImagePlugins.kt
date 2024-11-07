package ru.naviai.aiijc

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import kotlinx.serialization.Serializable
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.imgproc.Imgproc
import kotlin.math.pow


@Serializable
data class FiltersParams(
    val sharpness: Float = 0f,
    val brightness: Float = 0f,
    val saturation: Float = 1f,
    val iou: Float = 0.6f,
    val threshold: Float = 0.585f
)

fun FiltersParams.replace(
    sharpness: Float = this.sharpness,
    brightness: Float = this.brightness,
    saturation: Float = this.saturation,
    iou: Float = this.iou,
    threshold: Float = this.threshold
): FiltersParams {
    return FiltersParams(sharpness, brightness, saturation, iou, threshold)
}

fun adjustBitmap(
    bitmap: Bitmap,
    brightness: Float,
    saturation: Float,
    sharpness: Float
): Bitmap {
    // Create a ColorMatrix for brightness and saturation
    val colorMatrix = ColorMatrix()

    // Adjust brightness
    colorMatrix.setScale(1f, 1f, 1f, 1f) // Reset to identity
    colorMatrix.postConcat(
        ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, brightness / 100 * 255, // Red
                0f, 1f, 0f, 0f, brightness / 100 * 255, // Green
                0f, 0f, 1f, 0f, brightness / 100 * 255, // Blue
                0f, 0f, 0f, 1f, 0f                // Alpha
            )
        )
    )

    // Adjust saturation
    val saturationMatrix = ColorMatrix()
    saturationMatrix.setSaturation(saturation)
    colorMatrix.postConcat(saturationMatrix)

    // Create a new bitmap to hold the result
    val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config!!)
    val canvas = Canvas(resultBitmap)
    val paint = Paint()

    // Apply the color filter to the paint
    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)

    // Draw the original bitmap onto the new bitmap using the paint with the color filter
    canvas.drawBitmap(bitmap, 0f, 0f, paint)

    return applySharpnessFilter(resultBitmap, sharpness.toDouble())
}

private fun jpg2rgb888(img: Bitmap): Bitmap {
    val result: Bitmap?
    val numPixels = img.width * img.height
    val pixels = IntArray(numPixels)
    img.getPixels(pixels, 0, img.width, 0, 0, img.width, img.height)
    result = Bitmap.createBitmap(img.width, img.height, Bitmap.Config.ARGB_8888)
    result.setPixels(pixels, 0, result.width, 0, 0, result.width, result.height)
    return result
}

fun applySharpnessFilter(
    bitmap: Bitmap,
    k: Double
): Bitmap {
    val k0 = k / 100
    val k1 = 8 * k0 + 1
    val k2 = -1 * k0

    val bmp32 = jpg2rgb888(bitmap)

    val sourceMat = Mat()
    Utils.bitmapToMat(bmp32, sourceMat)

    val convMat = Mat(3, 3, CvType.CV_32F)

    convMat.put(0, 0, k2)
    convMat.put(0, 1, k2)
    convMat.put(0, 2, k2)
    convMat.put(1, 0, k2)
    convMat.put(1, 1, k1)
    convMat.put(1, 2, k2)
    convMat.put(2, 0, k2)
    convMat.put(2, 1, k2)
    convMat.put(2, 2, k2)

    Imgproc.filter2D(sourceMat, sourceMat, sourceMat.depth(), convMat)

    Utils.matToBitmap(sourceMat, bitmap)

    return bitmap
}


fun getSharpnessScore(bitmap: Bitmap): Double {
    val destination = Mat()
    val matGray = Mat()
    val sourceMatImage = Mat()
    Utils.bitmapToMat(bitmap, sourceMatImage)
    Imgproc.cvtColor(sourceMatImage, matGray, Imgproc.COLOR_BGR2GRAY)
    Imgproc.Laplacian(matGray, destination, 3)
    val median = MatOfDouble()
    val std = MatOfDouble()
    Core.meanStdDev(destination, median, std)
    return std.get(0, 0)[0].pow(2.0)
}

fun getBrightScore(bitmap: Bitmap): Double {
    val matGray = Mat()
    val sourceMatImage = Mat()
    Utils.bitmapToMat(bitmap, sourceMatImage)
    Imgproc.cvtColor(sourceMatImage, matGray, Imgproc.COLOR_BGR2HSV)

    val mean = MatOfDouble()
    val std = MatOfDouble()
    Core.meanStdDev(matGray, mean, std)

    return mean.get(2, 0)[0]
}