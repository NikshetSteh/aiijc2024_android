package ru.naviai.aiijc.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.image.cropview.CropType
import com.image.cropview.EdgeType
import com.image.cropview.ImageCrop
import ru.naviai.aiijc.ui.DropdownSelection
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun PhotoEditScreen(
    imageUri: Uri,
    navController: NavController
) {
    Log.i("kilo", "PhotoEditScreen")

    val context = LocalContext.current
    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)

    val bitmap = BitmapFactory.decodeStream(inputStream)

    Log.i("kilo", bitmap.height.toString())
    Log.i("kilo", bitmap.width.toString())

    val imageCrop = ImageCrop(bitmap)

    Box(modifier = Modifier.fillMaxSize()) {
        imageCrop.ImageCropView(
            modifier = Modifier.padding(4.dp),
            guideLineColor = Color.LightGray,
            guideLineWidth = 2.dp,
            edgeCircleSize = 5.dp,
            showGuideLines = true,
            cropType = CropType.FREE_STYLE,
            edgeType = EdgeType.CIRCULAR
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val profilesTypes = mutableMapOf<String, String>()
            profilesTypes["Круглый"] = "circle"

            val type = profilesTypes[DropdownSelection(
                data = profilesTypes.keys.toList(),
                default = profilesTypes.keys.toList()[0],
                label = "Тип профиля",
                modifier = Modifier.width(160.dp)
            )]

            Button(onClick = {
                val finalBitmap = imageCrop.onCrop()

                val file = File(context.cacheDir, "temp_image.png")
                val outputStream = FileOutputStream(file)
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

                val encodedUrl = URLEncoder.encode(
                    Uri.fromFile(file).toString(),
                    StandardCharsets.UTF_8.toString()
                )

                navController.navigate("result/$type/$encodedUrl")
            }) {
                Text("Далее")
            }
        }
    }
}
