package ru.naviai.aiijc

import android.graphics.Bitmap


fun scaleBitmapWithBlackMargins(
    bitmap: Bitmap,
    targetWidth: Int = 640,
    targetHeight: Int = 640
): Bitmap {
    // Calculate the aspect ratio of the original bitmap
    val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

    // Determine the new dimensions while maintaining the aspect ratio
    val (newWidth, newHeight) = if (aspectRatio > 1) {
        // Bitmap is wider than it is tall
        targetWidth to (targetWidth / aspectRatio).toInt()
    } else {
        // Bitmap is taller than it is wide or square
        (targetHeight * aspectRatio).toInt() to targetHeight
    }

    // Create a new bitmap with the target dimensions
    val scaledBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)

    // Create a canvas to draw the scaled bitmap
    val canvas = android.graphics.Canvas(scaledBitmap)

    // Draw the black background
    canvas.drawColor(android.graphics.Color.BLACK)

    // Calculate the position to center the bitmap
    val left = (targetWidth - newWidth) / 2
    val top = (targetHeight - newHeight) / 2

    // Draw the scaled bitmap onto the canvas
    canvas.drawBitmap(
        Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true),
        left.toFloat(),
        top.toFloat(),
        null
    )

    return scaledBitmap
}
