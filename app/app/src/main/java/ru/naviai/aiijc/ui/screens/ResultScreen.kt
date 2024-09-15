package ru.naviai.aiijc.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chaquo.python.Python
import java.io.InputStream


@Composable
fun ResultScreen(imageUri: Uri, navController: NavController) {
    Log.i("kilo", imageUri.toString())

    val py = Python.getInstance()
    val pyobj = py.getModule("main")
    val obj = pyobj.callAttr("main", imageUri.toString())

    val context = LocalContext.current
    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)

    val bitmap = BitmapFactory.decodeStream(inputStream).asImageBitmap()

    Box {
        Image(
            bitmap,
            "",
            modifier = Modifier.padding(4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                "Кол-во труб: $obj",
                color = Color.White
            )
            Button(
                onClick = { navController.navigate("home") },
            ) {
                Text("Назад")
            }
        }
    }
}

