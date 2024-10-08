package ru.naviai.aiijc.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.image.cropview.CropType
import com.image.cropview.EdgeType
import com.image.cropview.ImageCrop
import ru.naviai.aiijc.ui.SelectField
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
    val context = LocalContext.current
    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)

    val bitmap = BitmapFactory.decodeStream(inputStream)

    val imageCrop = ImageCrop(bitmap)
    var type by remember {
        mutableStateOf("Circle")
    }

    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            imageCrop.ImageCropView(
                modifier = Modifier
                    .padding(8.dp)
                    .width(300.dp)
                    .height((300 * 1.78).dp),
                guideLineColor = Color.LightGray,
                guideLineWidth = 2.dp,
                edgeCircleSize = 5.dp,
                showGuideLines = true,
                cropType = CropType.FREE_STYLE,
                edgeType = EdgeType.CIRCULAR,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            )
            {
                SelectField(
                    options = listOf("Circle"),
                    label = "Тип",
                    value = type,
                    onChange = {
                        type = it
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = {}) {
                    Text(text = "Назад")
                }

                Button(
                    onClick = {
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
}
