package ru.naviai.aiijc.ui.screens

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController


@Composable
fun ResultScreen(imageUri: Uri, navController: NavController) {
    Log.i("kilo", imageUri.toString())

    var isReady by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }


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
