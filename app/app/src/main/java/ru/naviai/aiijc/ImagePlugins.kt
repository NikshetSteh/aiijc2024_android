package ru.naviai.aiijc

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint


fun adjustBitmap(bitmap: Bitmap, brightness: Float, saturation: Float): Bitmap {
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

    return resultBitmap
}

fun applySharpeningFilter(
    bitmap: Bitmap,
    weight: Float
): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val outputBitmap = Bitmap.createBitmap(width, height, bitmap.config)

    val normalizedWeight = weight

    // Define the sharpening kernel
    val kernel = arrayOf(
        intArrayOf(0, -1 * normalizedWeight.toInt(), 0),
        intArrayOf(-1 * normalizedWeight.toInt(), (5 * normalizedWeight).toInt(), -1 * normalizedWeight.toInt()),
        intArrayOf(0, -1 * normalizedWeight.toInt(), 0)
    )

    // Apply the kernel to each pixel
    for (x in 1 until width - 1) {
        for (y in 1 until height - 1) {
            var r = 0
            var g = 0
            var b = 0

            // Convolve the kernel with the surrounding pixels
            for (kx in -1..1) {
                for (ky in -1..1) {
                    val pixel = bitmap.getPixel(x + kx, y + ky)
                    val kr = kernel[kx + 1][ky + 1]

                    r += Color.red(pixel) * kr
                    g += Color.green(pixel) * kr
                    b += Color.blue(pixel) * kr
                }
            }

            // Clamp the values to be between 0 and 255
            r = r.coerceIn(0, 255)
            g = g.coerceIn(0, 255)
            b = b.coerceIn(0, 255)

            // Set the new pixel value
            outputBitmap.setPixel(x, y, Color.rgb(r, g, b))
        }
    }

    return outputBitmap
}


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