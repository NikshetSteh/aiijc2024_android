package ru.naviai.aiijc.ui.fragments

import android.graphics.Bitmap
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.image.cropview.CropType
import com.image.cropview.EdgeType
import com.image.cropview.ImageCrop

@Composable
fun Crop(
    bitmap: Bitmap?
): ImageCrop? {
    val imageCrop = bitmap?.let { ImageCrop(it) }
    imageCrop?.ImageCropView(
        modifier = Modifier
            .width(320.dp)
            .height(320.dp),
        guideLineColor = Color.LightGray,
        guideLineWidth = 2.dp,
        edgeCircleSize = 5.dp,
        showGuideLines = true,
        cropType = CropType.FREE_STYLE,
        edgeType = EdgeType.CIRCULAR,
    )

    return imageCrop
}

@Composable
fun CropBottom(
    imageCrop: ImageCrop?,
    onCrop: (Bitmap) -> Unit
) {
    Button(
        onClick = {
            imageCrop?.onCrop()?.let { onCrop(it) }
        }
    ) {
        Text("Далее")
    }
}