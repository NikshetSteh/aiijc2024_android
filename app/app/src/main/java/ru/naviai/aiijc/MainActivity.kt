package ru.naviai.aiijc

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.naviai.aiijc.ui.MainWindow
import ru.naviai.aiijc.ui.theme.Aiijc2024Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences: SharedPreferences =
            this.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

        Log.i("kilo", sharedPreferences.getString("theme", "system2").toString())

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContent {
            val darkTheme = when {
                sharedPreferences.getString("theme", "system") == "Dark" -> true
                sharedPreferences.getString("theme", "system") == "Light" -> false
                else -> isSystemInDarkTheme()
            }

            Aiijc2024Theme(
                darkTheme = darkTheme
            ) {
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
                    MainWindow(darkTheme)
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

    override fun attachBaseContext(newBase: Context?) {
        val sharedPreferences: SharedPreferences =
            newBase!!.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

        val isDefaultRussian = newBase.resources.configuration.locales[0].language == "ru_RU"

        if (sharedPreferences.getString(
                "language",
                if(isDefaultRussian) "ru" else "en"
        ) == "ru") {
            super.attachBaseContext(ApplicationLanguageHelper.wrap(newBase, "ru"))
            return
        }

        super.attachBaseContext(ApplicationLanguageHelper.wrap(newBase, "en"))
    }
}


fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}


