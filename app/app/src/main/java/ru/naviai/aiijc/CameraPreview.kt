package ru.naviai.aiijc

import android.content.ContentValues.TAG
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

//@Composable
//fun CameraPreview(
//    controller: LifecycleCameraController,
//    modifier: Modifier = Modifier,
//
//) {
//    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
//    AndroidView(
//        factory = {
//            PreviewView(it).apply {
//                this.controller = controller
//                controller.bindToLifecycle(lifecycleOwner)
//            }
//        },
//        modifier = modifier
//    )
//}


@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FIT_CENTER
): ImageCapture {
    val cameraView = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val previewView = PreviewView(context).apply {
                this.scaleType = scaleType
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // Preview is incorrectly scaled in Compose on some devices without this
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                try {
                    // Must unbind the use-cases before rebinding them.
                    cameraProvider.unbindAll()

                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, cameraView
                    )
                } catch (exc: Exception) {
                    Log.e(TAG, "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(context))

            previewView
        })

    return cameraView
}