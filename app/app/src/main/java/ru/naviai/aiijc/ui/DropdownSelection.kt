package ru.naviai.aiijc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.ui.theme.Aiijc2024Theme

//@Composable
//fun DropdownSelection(
//    data: List<String>,
//    default: String,
//    label: String,
//    modifier: Modifier = Modifier
//) : String {
//    var expanded by remember { mutableStateOf(false) }
//    var selectedText by remember { mutableStateOf(default) }
//    var textFieldSize by remember { mutableStateOf(Size.Zero) }
//
//    val icon = if (expanded)
//        Icons.Filled.KeyboardArrowUp
//    else
//        Icons.Filled.KeyboardArrowDown
//
//    Column(modifier) {
//        TextField(
//            value = selectedText,
//            onValueChange = {},
//            modifier = Modifier
//                .fillMaxWidth()
//                .onGloballyPositioned { coordinates ->
//                    textFieldSize = coordinates.size.toSize()
//                },
//            label = { Text(label) },
//            trailingIcon = {
//                Icon(icon, "contentDescription",
//                    Modifier.clickable { expanded = !expanded })
//            },
//        )
//
//        // Create a drop-down menu with list of cities,
//        // when clicked, set the Text Field text as the city selected
//        DropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false },
//            modifier = Modifier
//                .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
//        ) {
//            data.forEach { label ->
//                DropdownMenuItem(
//                    text = { Text(text = label) },
//                    onClick = {
//                        selectedText = label
//                        expanded = false
//                    })
//            }
//        }
//    }
//
//    return selectedText
//}


@Composable
fun SelectField(
    modifier: Modifier = Modifier,
    label: String,
    options: List<String>,
    onChange: (String)->Unit,
    value: String,
    disabled: Boolean = false
) {
    var menuExpanded by remember {
        mutableStateOf(false)
    }
    val scrollState = rememberScrollState()
    Column(modifier = modifier) {
        val colors = OutlinedTextFieldDefaults.colors()
        OutlinedTextField(
            value = value,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                disabledBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
            ),
            enabled = false,
            modifier = Modifier.clickable {
                menuExpanded = !menuExpanded
            },
            onValueChange = {},
            label = {
                Text(text = label)
            },
            trailingIcon = {
                if (menuExpanded) {
                    IconButton(onClick = {
                        menuExpanded = false
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_drop_up),
                            contentDescription = "Close menu"
                        )
                    }
                } else {
                    IconButton(onClick = {
                        menuExpanded = true && !disabled
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_drop_down),
                            contentDescription = "Open menu"
                        )
                    }
                }
            },
            maxLines = 1
        )
        if (menuExpanded) {
            val customModifier = if (options.size > 5) {
                Modifier.height(235.dp)
            } else {
                Modifier
            }
            DropdownMenu(
                modifier = customModifier,
                expanded = menuExpanded,
//                scrollState = scrollState,
                onDismissRequest = {
                    menuExpanded = false
                },
                offset = DpOffset(x = 0.dp, y = 0.dp)
            ) {
                options.map {
                    val background = if(value == it) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                    DropdownMenuItem(
                        modifier = Modifier.background(background),
                        text = {
                            Text(text = it)
                        },
                        onClick = {
                            menuExpanded = false
                            onChange(it)
                        }
                    )
                }
            }
        }
    }
}