package ru.naviai.aiijc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import ru.naviai.aiijc.Item
import ru.naviai.aiijc.ui.fragments.Mode
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt


@Composable
fun ResultItem(
    title: String,
    size: Offset,
    fontSize: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    useBox: Boolean = false,
    enabled: Boolean = true,
    color: Color
) {
    with(LocalDensity.current) {
        Box(
            modifier,
            contentAlignment = Alignment.Center
        ) {
            if (useBox) {
                Box(
                    modifier = if (enabled) {
                        Modifier
                            .clickable(
                                onClick = onClick
                            )
                            .height(size.y.toDp())
                            .width(size.x.toDp())
                            .alpha(0.4f)
                            .background(color)
                    } else {
                        Modifier
                            .height(size.y.toDp())
                            .width(size.x.toDp())
                            .alpha(0.4f)
                            .background(color)
                    },
                ) {}
            } else {
                Card(
                    modifier = if (enabled) {
                        Modifier
                            .clickable(
                                onClick = onClick
                            )
                            .height(size.y.toDp())
                            .width(size.x.toDp())
                            .alpha(0.4f)
                    } else {
                        Modifier
                            .height(size.y.toDp())
                            .width(size.x.toDp())
                            .alpha(0.4f)
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = color
                    )
                ) {

                }
            }
            Text(
                text = title,
                fontSize = (0.16 * fontSize * 0.7.pow(max(title.length - 2, 0))).sp
            )
        }
    }
}


@Composable
fun ResultsItems(
    items: List<Item>,
    imageSize: IntOffset,
    size: Offset,
    onChange: (List<Item>) -> Unit,
    actionMode: Mode = Mode.NONE,
    isRectangle: Boolean
) {
    val colors = listOf(
        Color(240 , 128 , 128),
        Color(255 , 192 , 203),
        Color(255 , 160 , 122),
        Color(240 , 230 , 140),
        Color(255 , 218 , 185),
        Color(221 , 160 , 221),
        Color(255 , 222 , 173),
        Color(188 , 143 , 143),
        Color(0 , 128 , 128),
        Color(152 , 251 , 152),
        Color(102 , 205 , 170),
        Color(127 , 255 , 212),
        Color(95 , 158 , 160),
        Color(135 , 206 , 250),
        Color(123 , 104 , 238),
        Color(250 , 235 , 215),
        Color(255 , 228 , 225),
    )

    with(LocalDensity.current) {
        var counter = 1

        for (item in items) {
            ResultItem(
                (counter++).toString(),
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (-imageSize.x / 2 + item.offset.x.toFloat() / 640 * imageSize.x).roundToInt(),
                            (-imageSize.y / 2 + item.offset.y.toFloat() / 640 * imageSize.y).roundToInt()
                        )
                    }
                    .height(size.x.toDp())
                    .width(size.y.toDp()),
                size = size,
                fontSize = min(size.x, size.y),
                onClick = {
                    if (actionMode == Mode.DELETE) {
                        onChange(items - item)
                    }
                },
                useBox = isRectangle,
                enabled = actionMode == Mode.DELETE,
                color=colors[counter % colors.size]
            )
        }
    }
}