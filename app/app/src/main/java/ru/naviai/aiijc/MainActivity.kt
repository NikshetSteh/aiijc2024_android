package ru.naviai.aiijc

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.naviai.aiijc.ui.screens.MainScreen
import ru.naviai.aiijc.ui.theme.Aiijc2024Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Aiijc2024Theme {
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
                    MainScreen()
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


fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}


