package ru.naviai.aiijc.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.NaviAI.aiijc.R

@Composable
fun AboutScreen(
    onExit: () -> Unit
) {
    val resources = LocalContext.current.resources

    Column {
        IconButton(onClick = onExit) {
            Icon(Icons.Outlined.ArrowBack, "back")
        }

        Column (
            horizontalAlignment = CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painterResource(id = R.drawable.main_icon),
                contentDescription = "AppIcon",
                modifier = Modifier
                    .height(128.dp)
                    .padding(16.dp)
            )

            Text(resources.getString(R.string.app_name))

            Text(
                text = "NaviAI team, specifically for Aiijc 2024"
            )
        }
    }
}