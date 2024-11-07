package ru.naviai.aiijc

import android.content.Context
import android.graphics.Bitmap
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*


@Serializable
data class HistoryItem(
    val modelResults: ModelResults,
    var datetime: String = "",
    val filtersParams: FiltersParams,
    val imageRect: ImageRect
)


fun saveResultsWithImageByDate(
    context: Context,
    item: HistoryItem,
    initialBitmap: Bitmap,
    filteredBitmap: Bitmap
) {
    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

    item.datetime = currentDate + timestamp

    val imageInitialFile = File(
        context.cacheDir,
        "history/${item.datetime}/${timestamp}_initial.png"
    )
    FileOutputStream(imageInitialFile).use { out ->
        initialBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    val imageFilteredFile = File(
        context.cacheDir,
        "history/${item.datetime}/${timestamp}_filtered.png"
    )
    FileOutputStream(imageFilteredFile).use { out ->
        filteredBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }

    val resultsFile = File(
        context.cacheDir,
        "history/${item.datetime}/$timestamp.ser"
    )
    FileWriter(resultsFile).use { writer ->
        writer.write(Json.encodeToString(item))
    }
}

fun loadHistory(context: Context): MutableList<ModelResults> {
    val historyDir = File(context.cacheDir, "history")
    val resultsList = mutableListOf<ModelResults>()

    if (historyDir.exists() && historyDir.isDirectory) {
        // Iterate through files in the date directory
        historyDir.listFiles()?.forEach { file ->
            if (file.extension == "ser") {
                FileReader(file).use { reader ->
                    resultsList.add(Json.decodeFromString(reader.readText()))
                }
            }
        }
    }

    return resultsList
}