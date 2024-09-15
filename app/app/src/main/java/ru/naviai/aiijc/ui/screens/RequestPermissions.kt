package ru.naviai.aiijc.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@Composable
fun CameraPermissionScreen(navController: NavController) {
    Log.i("kilo", "Check permissions")

    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }
    var alreadyRequestPermission by remember { mutableStateOf(false) }

    // Launcher to request camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            run {
                permissionGranted = isGranted
                alreadyRequestPermission = true
            }
        }
    )

    // Check if permission is already granted
    when (PackageManager.PERMISSION_GRANTED) {
        context.checkSelfPermission(Manifest.permission.CAMERA) -> {
            permissionGranted = true
        }

        else -> {
            // Request permission
            SideEffect {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // Navigate to home if permission is granted
    if (permissionGranted) {
        navController.navigate("home")
    }

    // UI for requesting permission
    if (!permissionGranted && alreadyRequestPermission) {
        Text("Camera permission is required to proceed.")
    }
}