package ru.naviai.aiijc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import ru.NaviAI.aiijc.R


@Composable
fun SelectField(
    modifier: Modifier = Modifier,
    label: String,
    options: List<String>,
    onChange: (String) -> Unit,
    value: String,
    disabled: Boolean = false
) {
    var menuExpanded by remember {
        mutableStateOf(false)
    }
    rememberScrollState()
    Column(modifier = modifier) {
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
                    val background = if (value == it) {
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