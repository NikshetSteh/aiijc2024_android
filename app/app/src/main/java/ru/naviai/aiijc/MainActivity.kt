package ru.naviai.aiijc

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import ru.naviai.aiijc.ui.screens.CameraPermissionScreen
import ru.naviai.aiijc.ui.screens.CameraScreen
import ru.naviai.aiijc.ui.screens.PhotoEditScreen
import ru.naviai.aiijc.ui.screens.ResultScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                var pythonStarted by remember { mutableStateOf(false) }

                if (!pythonStarted) {
                    val context = LocalContext.current
                    Python.start(AndroidPlatform(context))
                    pythonStarted = true
                }

                val viewModel = viewModel<PermissionViewModel>()
                val dialogQueue = viewModel.visiblePermissionDialogQueue
                var isRequested = false

                val cameraPermissionResultLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        viewModel.onPermissionResult(
                            permission = Manifest.permission.CAMERA,
                            isGranted = isGranted
                        )
                    }
                )

                dialogQueue
                    .reversed()
                    .forEach { permission ->
                        isRequested = true
                        PermissionDialog(
                            descriptionText = "Камера необходима для работы приложения",
                            isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                                permission
                            ),
                            onDismiss = viewModel::dismissDialog,
                            onOkClick = {
                                viewModel.dismissDialog()
                                cameraPermissionResultLauncher.launch(
                                    permission
                                )
                            },
                            onGoToAppSettingsClick = ::openAppSettings
                        )
                    }

                if (!hasRequiredPermissions() && !isRequested) {
                    SideEffect {
                        cameraPermissionResultLauncher.launch(
                            Manifest.permission.CAMERA
                        )
                    }
                }


                Surface {
                    val navController = rememberNavController()
                    val cameraController = remember {
                        LifecycleCameraController(applicationContext).apply {
                            setEnabledUseCases(
                                CameraController.IMAGE_CAPTURE or
                                        CameraController.VIDEO_CAPTURE
                            )
                        }
                    }
                    AppNavHost(navController, applicationContext, cameraController)
                }
            }
        }
    }


    private fun hasRequiredPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    applicationContext: Context,
    cameraController: LifecycleCameraController
) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { CameraScreen(applicationContext, navController, cameraController) }
        composable("crop/{imageUri}") { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri")
            imageUri?.let {
                PhotoEditScreen(
                    it.toUri(),
                    navController
                )
            }
        }
        composable("result/{type}/{imageUri}") { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri")
//            val type = backStackEntry.arguments?.getString("type")
            imageUri?.let {
                ResultScreen(
                    it.toUri(),
                    navController
                )
            }
        }
        composable("permission") {
            CameraPermissionScreen(navController)
        }
    }
}


fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}


