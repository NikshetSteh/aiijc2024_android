package ru.naviai.aiijc.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import kotlinx.coroutines.launch
import ru.naviai.aiijc.ui.screens.MainScreen
import ru.naviai.aiijc.ui.screens.SettingsScreen

enum class State {
    Main, Settings, Guide, About
}

@Composable
fun MainWindow() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var state by remember {
        mutableStateOf(State.Main)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet{
                IconButton(onClick = {
                    scope.launch {
                        drawerState.apply {
                            if (isClosed) open() else close()
                        }
                    }
                }) {
                    Icon(Icons.Outlined.Menu, contentDescription = "Menu")
                }

                NavigationDrawerItem(
                    label = { Text("Main") },
                    selected = state == State.Main,
                    onClick = { state = State.Main }
                )
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = state == State.Settings,
                    onClick = { state = State.Settings }
                )
                NavigationDrawerItem(
                    label = { Text("Guide") },
                    selected = state == State.Guide,
                    onClick = { state = State.Guide }
                )
                NavigationDrawerItem(
                    label = { Text("About") },
                    selected = state == State.About,
                    onClick = { state = State.About }
                )
            }
        }
    )
    {
        when (state) {
            State.Main -> MainScreen(scope, drawerState)
            State.Settings -> SettingsScreen()
//            State.Guide -> GuideScreen()
//            State.About -> AboutScreen()
            else -> {}
        }
//        MainScreen(
//            scope = scope,
//            drawerState = drawerState
//        )
    }
}


