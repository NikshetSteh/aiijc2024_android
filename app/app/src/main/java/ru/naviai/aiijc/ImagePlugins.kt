package ru.naviai.aiijc

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.pow


fun contrast(src: Bitmap, value: Double): Bitmap {
    // image size
    val width = src.width
    val height = src.height
    // create output bitmap
    val bmOut = Bitmap.createBitmap(width, height, src.config)
    // color information
    var colorA: Int
    var colorR: Int
    var colorG: Int
    var colorB: Int
    var pixel: Int
    // get contrast value
    val contrast = ((100 + value) / 100).pow(2.0)

    // scan through all pixels
    for (x in 0 until width) {
        for (y in 0 until height) {
            // get pixel color
            pixel = src.getPixel(x, y)
            colorA = Color.alpha(pixel)
            // apply filter contrast for every channel R, G, B
            colorR = Color.red(pixel)
            colorR = (((colorR / 255.0 - 0.5) * contrast + 0.5) * 255.0).toInt()
            if (colorR < 0) {
                colorR = 0
            } else if (colorR > 255) {
                colorR = 255
            }
            colorG = Color.red(pixel)
            colorG = (((colorG / 255.0 - 0.5) * contrast + 0.5) * 255.0).toInt()
            if (colorG < 0) {
                colorG = 0
            } else if (colorG > 255) {
                colorG = 255
            }
            colorB = Color.red(pixel)
            colorB = (((colorB / 255.0 - 0.5) * contrast + 0.5) * 255.0).toInt()
            if (colorB < 0) {
                colorB = 0
            } else if (colorB > 255) {
                colorB = 255
            }

            // set new pixel color to output bitmap
            bmOut.setPixel(x, y, Color.argb(colorA, colorR, colorG, colorB))
        }
    }

    // return final image
    return bmOut
}


fun sharpness(src: Bitmap, weight: Double): Bitmap {
    val sharpConfig = arrayOf(
        doubleArrayOf(0.0, -2.0, 0.0),
        doubleArrayOf(-2.0, weight, -2.0),
        doubleArrayOf(0.0, -2.0, 0.0)
    )
    val convMatrix = ConvolutionMatrix(3)
    convMatrix.applyConfig(sharpConfig)
    convMatrix.factor = weight - 8

    return ConvolutionMatrix.computeConvolution3x3(src, convMatrix)
}

fun brightness(src: Bitmap, value: Double): Bitmap {
    // image size
    val width = src.width
    val height = src.height
    // create output bitmap
    val bmOut = Bitmap.createBitmap(width, height, src.config)
    // color information
    var colorA: Int
    var colorR: Int
    var colorG: Int
    var colorB: Int
    var pixel: Int

    // scan through all pixels
    for (x in 0 until width) {
        for (y in 0 until height) {
            // get pixel color
            pixel = src.getPixel(x, y)
            colorA = Color.alpha(pixel)
            colorR = Color.red(pixel)
            colorG = Color.green(pixel)
            colorB = Color.blue(pixel)

            // increase/decrease each channel
            colorR = (colorR +  value).toInt()
            if (colorR > 255) {
                colorR = 255
            } else if (colorR < 0) {
                colorR = 0
            }
            colorG = (colorG + value).toInt()
            if (colorG > 255) {
                colorG = 255
            } else if (colorG < 0) {
                colorG = 0
            }
            colorB = (colorB + value).toInt()
            if (colorB > 255) {
                colorB = 255
            } else if (colorB < 0) {
                colorB = 0
            }

            bmOut.setPixel(x, y, Color.argb(colorA, colorR, colorG, colorB))
        }
    }

    return bmOut
}



class ConvolutionMatrix(size: Int) {
    var matrix: Array<DoubleArray>
    var factor = 1.0
    var offset = 1.0

    //Constructor with argument of size
    init {
        matrix = Array(size) { DoubleArray(size) }
    }

    fun applyConfig(config: Array<DoubleArray>) {
        for (x in 0 until SIZE) {
            for (y in 0 until SIZE) {
                matrix[x][y] = config[x][y]
            }
        }
    }

    companion object {
        const val SIZE = 3
        fun computeConvolution3x3(src: Bitmap, matrix: ConvolutionMatrix): Bitmap {
            val width = src.width
            val height = src.height
            val result = Bitmap.createBitmap(width, height, src.config)
            var colorA: Int
            var colorR: Int
            var colorG: Int
            var colorB: Int
            var sumR: Int
            var sumG: Int
            var sumB: Int
            val pixels = Array(SIZE) {
                IntArray(
                    SIZE
                )
            }
            for (y in 0 until height - 2) {
                for (x in 0 until width - 2) {

                    // get pixel matrix
                    for (i in 0 until SIZE) {
                        for (j in 0 until SIZE) {
                            pixels[i][j] = src.getPixel(x + i, y + j)
                        }
                    }

                    // get alpha of center pixel
                    colorA = Color.alpha(pixels[1][1])

                    // init color sum
                    sumB = 0
                    sumG = 0
                    sumR = 0

                    // get sum of RGB on matrix
                    for (i in 0 until SIZE) {
                        for (j in 0 until SIZE) {
                            sumR = (sumR + Color.red(pixels[i][j]) * matrix.matrix[i][j]).toInt()
                            sumG = (sumG + Color.green(pixels[i][j]) * matrix.matrix[i][j]).toInt()
                            sumB = (sumB + Color.blue(pixels[i][j]) * matrix.matrix[i][j]).toInt()
                        }
                    }

                    // get final Red
                    colorR = (sumR / matrix.factor + matrix.offset).toInt()
                    if (colorR < 0) {
                        colorR = 0
                    } else if (colorR > 255) {
                        colorR = 255
                    }

                    // get final Green
                    colorG = (sumG / matrix.factor + matrix.offset).toInt()
                    if (colorG < 0) {
                        colorG = 0
                    } else if (colorG > 255) {
                        colorG = 255
                    }

                    // get final Blue
                    colorB = (sumB / matrix.factor + matrix.offset).toInt()
                    if (colorB < 0) {
                        colorB = 0
                    } else if (colorB > 255) {
                        colorB = 255
                    }

                    // apply new pixel
                    result.setPixel(x + 1, y + 1, Color.argb(colorA, colorR, colorG, colorB))
                }
            }

            // final image
            return result
        }
    }
}