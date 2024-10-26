package ru.naviai.aiijc.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.ui.screens.AboutScreen
import ru.naviai.aiijc.ui.screens.MainScreen
import ru.naviai.aiijc.ui.screens.SettingsScreen
import ru.naviai.aiijc.ui.theme.Aiijc2024Theme

enum class State {
    Main, Settings, About
}

@Composable
fun MainWindow(
    darkTheme: Boolean
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val resources = LocalContext.current.resources

    var state by remember {
        mutableStateOf(State.Main)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                IconButton(onClick = {
                    scope.launch {
                        drawerState.apply {
                            if (isClosed) open() else close()
                        }
                    }
                }) {
                    Icon(Icons.Outlined.Menu, contentDescription = "Menu")
                }

                Row {
                    Image(
                        if (darkTheme) painterResource(id = R.drawable.main_icon_light) else painterResource(id = R.drawable.main_icon),
                        contentDescription = "AppIcon",
                        modifier = Modifier
                            .height(128.dp)
                            .padding(16.dp)
                    )
                    Column(
                        modifier = Modifier
                            .height(128.dp)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            resources.getString(R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                }

                Divider(thickness = 1.dp)

                Spacer(modifier = Modifier.height(16.dp))

                NavigationDrawerItem(
                    label = {
                        Row {
                            Icon(
                                imageVector = Icons.Outlined.Home,
                                contentDescription = "Main"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(resources.getString(R.string.label_main))
                        }
                    },
                    selected = true,
                    onClick = {
                        state = State.Main
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
                NavigationDrawerItem(
                    label = {
                        Row {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(resources.getString(R.string.label_settings))
                        }
                    },
                    selected = false,
                    onClick = {
                        state = State.Settings
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
                NavigationDrawerItem(
                    label = {
                        Row {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Info"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(resources.getString(R.string.label_about))
                        }
                    },
                    selected = false,
                    onClick = {
                        state = State.About
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        }
    )
    {
        when (state) {
            State.Main ->
                Aiijc2024Theme(
                    darkTheme = true
                ) {
                    MainScreen(scope, drawerState)
                }
            State.Settings -> SettingsScreen {
                state = State.Main
            }
            State.About -> AboutScreen {
                state = State.Main
            }
        }
    }
}


