package ru.naviai.aiijc.ui.screens

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.ui.SelectField
import java.util.*


@Composable
fun SettingsScreen() {
    val resources = LocalContext.current.resources

    val themes = listOf(
        resources.getString(R.string.theme_dark),
        resources.getString(R.string.theme_light),
        resources.getString(R.string.theme_system),
    )
    val languages = listOf("Russian", "English")


    var loadedPrevious by remember {
        mutableStateOf(false)
    }
    var selectedTheme by remember { mutableStateOf(themes[0]) }
    var selectedLanguage by remember { mutableStateOf(languages[0]) }

    if (!loadedPrevious) {
        val sharedPreferences: SharedPreferences =
            LocalContext.current.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

        selectedTheme = sharedPreferences.getString("theme", "System").toString()
        selectedTheme = when (selectedTheme) {
            "Dark" -> themes[0]
            "Light" -> themes[1]
            else -> themes[2]
        }

        val configuration: Configuration = LocalContext.current.resources.configuration

        val current: Locale = configuration.locales[0]

        selectedLanguage = when (current.language) {
            "ru" -> "Russian"
            "en" -> "English"
            else -> "Error"
        }

        loadedPrevious = true
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        SelectField(
            label = resources.getString(R.string.label_theme),
            options = themes,
            onChange = {
                selectedTheme = it
            },
            value = selectedTheme
        )

        Spacer(modifier = Modifier.height(16.dp))

        SelectField(
            label = resources.getString(R.string.label_language),
            options = languages,
            onChange = {
                selectedLanguage = it
            },
            value = selectedLanguage
        )

        val context = LocalContext.current

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

            val themeId = when (selectedTheme) {
                themes[0] -> "Dark"
                themes[1] -> "Light"
                else -> "System"
            }

            sharedPreferences.edit().putString("theme", themeId).apply()

            when (selectedLanguage) {
                "Russian" -> updateResources(context, "ru")
                "English" -> updateResources(context, "en")
                else -> updateResources(context, "en")
            }
        }) {
            Text(text = resources.getString(R.string.action_save))
        }
    }
}


private fun updateResources(context: Context, language: String) {
    val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("language", language).apply()
}
