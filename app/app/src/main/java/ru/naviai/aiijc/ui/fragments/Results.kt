package ru.naviai.aiijc.ui.fragments

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun Results(
    resultBitmap: Bitmap?
) {
    if (resultBitmap != null) {
        Image(
            bitmap = resultBitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .width(320.dp)
                .height(320.dp)
        )
    } else {
        Box(
            modifier = Modifier
                .width(320.dp)
                .height(320.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
            )
        }
    }
}


@Composable
fun ResultsBottom(
    onRestart: () -> Unit,
    onNewImage: () -> Unit,
    isLoading: Boolean,
    count: Int?
) {
    Button(
        onClick = onRestart,
        enabled = !isLoading
    ) {
        Text("Restart")
    }

    Box(modifier = Modifier.padding(14.dp)) {
        Text(
            text = if (count != null) "$count" else ".....",
            modifier = Modifier
                .width(60.dp)
                .wrapContentHeight(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1,
        )
    }

    Button(
        onClick = onNewImage,
        enabled = !isLoading
    ) {
        Text("New image")
    }
}