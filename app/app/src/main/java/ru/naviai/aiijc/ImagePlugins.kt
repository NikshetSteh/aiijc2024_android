package ru.naviai.aiijc

import android.R.attr
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


class FiltersParams(
    val sharpness: Float = 0f,
    val brightness: Float = 0f,
    val saturation: Float = 1f,
    val iou: Float = 0.6f,
    val threshold: Float = 0.2f
)


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
    val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
    val canvas = Canvas(resultBitmap)
    val paint = Paint()

    // Apply the color filter to the paint
    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)

    // Draw the original bitmap onto the new bitmap using the paint with the color filter
    canvas.drawBitmap(bitmap, 0f, 0f, paint)

    return applySharpnessFilter(resultBitmap, sharpness.toDouble())
}

private fun JPGtoRGB888(img: Bitmap): Bitmap {
    val result: Bitmap?
    val numPixels = img.width * img.height
    val pixels = IntArray(numPixels)
    //        get jpeg pixels, each int is the color value of one pixel
    img.getPixels(pixels, 0, img.width, 0, 0, img.width, img.height)
    //        create bitmap in appropriate format
    result = Bitmap.createBitmap(img.width, img.height, Bitmap.Config.ARGB_8888)
    //        Set RGB pixels
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

    val bmp32 = JPGtoRGB888(bitmap)

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

    Imgproc.filter2D(sourceMat, sourceMat, sourceMat.depth(), convMat);

    Utils.matToBitmap(sourceMat, bitmap)

    return bitmap
}

//fun applySharpeningFilter(
//    bitmap: Bitmap,
//    weight: Float
//): Bitmap {
//    val width = bitmap.width
//    val height = bitmap.height
//    val outputBitmap = Bitmap.createBitmap(width, height, bitmap.config)
//
//    // Define the sharpening kernel
//    val kernel = arrayOf(
//        intArrayOf(0, -1 * weight.toInt(), 0),
//        intArrayOf(
//            -1 * weight.toInt(),
//            (5 * weight).toInt(),
//            -1 * weight.toInt()
//        ),
//        intArrayOf(0, -1 * weight.toInt(), 0)
//    )
//
//    // Apply the kernel to each pixel
//    for (x in 1 until width - 1) {
//        for (y in 1 until height - 1) {
//            var r = 0
//            var g = 0
//            var b = 0
//
//            // Convolve the kernel with the surrounding pixels
//            for (kx in -1..1) {
//                for (ky in -1..1) {
//                    val pixel = bitmap.getPixel(x + kx, y + ky)
//                    val kr = kernel[kx + 1][ky + 1]
//
//                    r += Color.red(pixel) * kr
//                    g += Color.green(pixel) * kr
//                    b += Color.blue(pixel) * kr
//                }
//            }
//
//            // Clamp the values to be between 0 and 255
//            r = r.coerceIn(0, 255)
//            g = g.coerceIn(0, 255)
//            b = b.coerceIn(0, 255)
//
//            // Set the new pixel value
//            outputBitmap.setPixel(x, y, Color.rgb(r, g, b))
//        }
//    }
//
//    return outputBitmap
//}
//
//fun applySharpnessFilter(
//    bitmap: Bitmap,
//    k: Float
//): Bitmap {
//    val height: Int = bitmap.height
//    val width: Int = bitmap.width
//    var red: Int
//    var green: Int
//    var blue: Int
//    var a1: Int
//    var a2: Int
//    var a3: Int
//    var a4: Int
//    var a5: Int
//    var a6: Int
//    var a7: Int
//    var a8: Int
//    var a9: Int
//    val bmpBlurred = Bitmap.createBitmap(bitmap)
//
//    val canvas = Canvas(bmpBlurred)
//
//    canvas.drawBitmap(bitmap, Matrix(), null)
//    for (i in 1 until width - 1) {
//        for (j in 1 until height - 1) {
//            a1 = bitmap.getPixel(i - 1, j - 1)
//            a2 = bitmap.getPixel(i - 1, j)
//            a3 = bitmap.getPixel(i - 1, j + 1)
//            a4 = bitmap.getPixel(i, j - 1)
//            a5 = bitmap.getPixel(i, j)
//            a6 = bitmap.getPixel(i, j + 1)
//            a7 = bitmap.getPixel(i + 1, j - 1)
//            a8 = bitmap.getPixel(i + 1, j)
//            a9 = bitmap.getPixel(i + 1, j + 1)
//            red =
//                ((
//                        Color.red(a1) + Color.red(a2) + Color.red(a3) + Color.red(a4) +
//                                Color.red(a6) + Color.red(a7) + Color.red(a8) + Color.red(a9)
//                        ) * (-1 * k) + Color.red(a5) * (8 * k + 1)).roundToInt()
//            green =
//                ((
//                        Color.green(a1) + Color.green(a2) + Color.green(a3) + Color.green(a4) +
//                                Color.green(a6) + Color.green(a7) + Color.green(a8) + Color.green(a9)
//                        ) * (-1 * k) + Color.green(a5) * (8 * k + 1)).roundToInt()
//            blue =
//                ((
//                        Color.blue(a1) + Color.blue(a2) + Color.blue(a3) + Color.blue(a4)
//                                + Color.blue(a6) + Color.blue(a7) + Color.blue(a8) + Color.blue(a9)
//                        ) * (-1 * k) + Color.blue(a5) * (8 * k + 1)).roundToInt()
//
//
//            if(red > 255) red = 255
//            else if (red < 0) red = 0
//            if(green > 255) green = 255
//            else if (green < 0) green = 0
//            if(blue > 255) blue = 255
//            else if (blue < 0) blue = 0
//
//            bmpBlurred.setPixel(i, j, Color.rgb(red, green, blue))
//        }
//    }
//    return bmpBlurred
//}


//fun sharpness(src: Bitmap, weight: Double): Bitmap {
//    val sharpConfig = arrayOf(
//        doubleArrayOf(0.0, -2.0, 0.0),
//        doubleArrayOf(-2.0, weight, -2.0),
//        doubleArrayOf(0.0, -2.0, 0.0)
//    )
//    val convMatrix = ConvolutionMatrix(3)
//    convMatrix.applyConfig(sharpConfig)
//    convMatrix.factor = weight - 8
//
//    return ConvolutionMatrix.computeConvolution3x3(src, convMatrix)
//}


//class ConvolutionMatrix(size: Int) {
//    var matrix: Array<DoubleArray>
//    var factor = 1.0
//    var offset = 1.0
//
//    //Constructor with argument of size
//    init {
//        matrix = Array(size) { DoubleArray(size) }
//    }
//
//    fun applyConfig(config: Array<DoubleArray>) {
//        for (x in 0 until SIZE) {
//            for (y in 0 until SIZE) {
//                matrix[x][y] = config[x][y]
//            }
//        }
//    }
//
//    companion object {
//        const val SIZE = 3
//        fun computeConvolution3x3(src: Bitmap, matrix: ConvolutionMatrix): Bitmap {
//            val width = src.width
//            val height = src.height
//            val result = Bitmap.createBitmap(width, height, src.config)
//            var colorA: Int
//            var colorR: Int
//            var colorG: Int
//            var colorB: Int
//            var sumR: Int
//            var sumG: Int
//            var sumB: Int
//            val pixels = Array(SIZE) {
//                IntArray(
//                    SIZE
//                )
//            }
//            for (y in 0 until height - 2) {
//                for (x in 0 until width - 2) {
//
//                    // get pixel matrix
//                    for (i in 0 until SIZE) {
//                        for (j in 0 until SIZE) {
//                            pixels[i][j] = src.getPixel(x + i, y + j)
//                        }
//                    }
//
//                    // get alpha of center pixel
//                    colorA = Color.alpha(pixels[1][1])
//
//                    // init color sum
//                    sumB = 0
//                    sumG = 0
//                    sumR = 0
//
//                    // get sum of RGB on matrix
//                    for (i in 0 until SIZE) {
//                        for (j in 0 until SIZE) {
//                            sumR = (sumR + Color.red(pixels[i][j]) * matrix.matrix[i][j]).toInt()
//                            sumG = (sumG + Color.green(pixels[i][j]) * matrix.matrix[i][j]).toInt()
//                            sumB = (sumB + Color.blue(pixels[i][j]) * matrix.matrix[i][j]).toInt()
//                        }
//                    }
//
//                    // get final Red
//                    colorR = (sumR / matrix.factor + matrix.offset).toInt()
//                    if (colorR < 0) {
//                        colorR = 0
//                    } else if (colorR > 255) {
//                        colorR = 255
//                    }
//
//                    // get final Green
//                    colorG = (sumG / matrix.factor + matrix.offset).toInt()
//                    if (colorG < 0) {
//                        colorG = 0
//                    } else if (colorG > 255) {
//                        colorG = 255
//                    }
//
//                    // get final Blue
//                    colorB = (sumB / matrix.factor + matrix.offset).toInt()
//                    if (colorB < 0) {
//                        colorB = 0
//                    } else if (colorB > 255) {
//                        colorB = 255
//                    }
//
//                    // apply new pixel
//                    result.setPixel(x + 1, y + 1, Color.argb(colorA, colorR, colorG, colorB))
//                }
//            }
//
//            // final image
//            return result
//        }
//    }
//}