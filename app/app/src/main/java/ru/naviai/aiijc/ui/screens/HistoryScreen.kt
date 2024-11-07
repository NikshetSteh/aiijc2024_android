package ru.naviai.aiijc.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.NaviAI.aiijc.R
import ru.naviai.aiijc.FiltersParams
import ru.naviai.aiijc.HistoryItem
import ru.naviai.aiijc.HistoryItemCombineData
import ru.naviai.aiijc.ImageRect
import ru.naviai.aiijc.IntOffsetSerializable
import ru.naviai.aiijc.Item
import ru.naviai.aiijc.Model
import ru.naviai.aiijc.ModelResults
import ru.naviai.aiijc.loadHistory
import ru.naviai.aiijc.toSerializable
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryItemView(
    item: HistoryItem,
    filteredBitmap: Bitmap,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Card(
                modifier = Modifier
                    .height(100.dp)
                    .width(100.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Gray
                )
            ) {
                Image(
                    filteredBitmap.asImageBitmap(),
                    contentDescription = null
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.Start,

                ) {
                    val typeTitle = when(item.type) {
                        Model.PredictionsType.ALL -> stringResource(R.string.type_all)
                        Model.PredictionsType.CIRCLE -> stringResource(R.string.type_circle)
                        Model.PredictionsType.RECTANGLE -> stringResource(R.string.type_rectangle)
                    }

                    Text(
                        "${stringResource(R.string.label_type)}: $typeTitle",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("${stringResource(R.string.label_count)}: ${item.modelResults.count}")
                }
                Text(
                    item.datetime,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .padding(
                            vertical = 8.dp,
                            horizontal = 16.dp
                        )
                )
            }
        }
    }
}


fun sortElementsByDateTime(elements: List<HistoryItemCombineData>): List<HistoryItemCombineData> {
    // Define the date format
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // Sort the elements based on the parsed date
    return elements.sortedByDescending { dateFormat.parse(it.item.datetime) }
}

@Composable
fun History(
    onLoad: (
            initialBitmap: Bitmap,
            filtersParams: FiltersParams,
            imageRect: ImageRect,
            type: Model.PredictionsType
            ) -> Unit,
    onBack: () -> Unit
) {
    val isLoaded = false
    var data = remember {
        mutableListOf<HistoryItemCombineData>()
    }

    if(!isLoaded) {
        val context = LocalContext.current
        data = loadHistory(context)
    }

    val scrollState = rememberScrollState()

    Scaffold (
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Filled.ExitToApp,
                        null
                    )
                }
            }
        }
    ) {
        Column (
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(it),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            sortElementsByDateTime(data).forEach { item ->
                HistoryItemView(
                    item = item.item,
                    filteredBitmap = item.filteredBitmap,
                    onClick = {
                        onLoad(
                            item.initialBitmap,
                            item.item.filtersParams,
                            item.item.imageRect,
                            item.item.type
                        )
                    }
                )
            }
        }
    }
}


@Preview
@Composable
fun HistoryItemScreenPreview() {
    val scrollState = rememberScrollState()

    Scaffold {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(it)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                HistoryItemView(
                    HistoryItem(
                        ModelResults(
                            50,
                            listOf(Item(IntOffset.Zero.toSerializable())),
                            Offset.Zero.toSerializable()
                        ),
                        "2024-11-08 16:35:87",
                        imageRect = ImageRect(
                            IntOffset.Zero.toSerializable(),
                            IntOffsetSerializable(100, 100)
                        ),
                        filtersParams = FiltersParams(),
                        type = Model.PredictionsType.CIRCLE
                    ),
                    Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888),
                    onClick = { }
                )
            }
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                HistoryItemView(
                    HistoryItem(
                        ModelResults(
                            50,
                            listOf(Item(IntOffset.Zero.toSerializable())),
                            Offset.Zero.toSerializable()
                        ),
                        "2024-11-08 16:35:87",
                        imageRect = ImageRect(
                            IntOffset.Zero.toSerializable(),
                            IntOffsetSerializable(100, 100)
                        ),
                        filtersParams = FiltersParams(),
                        type = Model.PredictionsType.CIRCLE
                    ),
                    Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888),
                    onClick = { }
                )
            }
        }
    }
}